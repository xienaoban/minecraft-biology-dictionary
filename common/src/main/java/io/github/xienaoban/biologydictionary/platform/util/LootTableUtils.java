package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.mixin.loot.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SequenceFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.UniformGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public final class LootTableUtils {
    private static final int MAX_RECURSION_DEPTH = 3;

    public static boolean hasLootTable(Entity entity) {
        return getLootTableKey(entity).isPresent();
    }

    public static Optional<ResourceKey<LootTable>> getLootTableKey(Entity entity) {
        return entity.getLootTable();
    }

    public static List<LootPool> getPools(LootTable lootTable) {
        return ((LootTableIMixin) lootTable).biologydictionary$getPools();
    }

    public static List<LootPoolEntryContainer> getEntries(LootPool pool) {
        return ((LootPoolIMixin) pool).biologydictionary$getEntries();
    }

    public static Holder<Item> getItem(LootItem lootItem) {
        return ((LootItemIMixin) lootItem).biologydictionary$getItem();
    }

    public static int getWeight(UniformContainerBase singleton) {
        return ((UniformContainerBaseIMixin) singleton).biologydictionary$getWeight();
    }

    public static List<LootItemFunction> getFunctions(UniformContainerBase singleton) {
        Optional<Holder<LootItemFunction>> modifier =
                ((LootPoolEntryContainerIMixin) singleton).biologydictionary$getModifier();
        if (modifier.isEmpty()) {
            return List.of();
        }

        LootItemFunction function = modifier.get().value();
        if (function instanceof SequenceFunction sequence) {
            HolderSet<LootItemFunction> functions = ((SequenceFunctionIMixin) sequence).biologydictionary$getFunctions();
            List<LootItemFunction> result = new ArrayList<>(functions.size());
            for (Holder<LootItemFunction> holder : functions) {
                result.add(holder.value());
            }
            return result;
        }
        return List.of(function);
    }

    public static HolderSet<Item> getTag(TagEntry tagEntry) {
        return ((TagEntryIMixin) tagEntry).biologydictionary$getTag();
    }

    public static HolderSet<LootTable> getContents(NestedLootTable nestedLoot) {
        return ((NestedLootTableIMixin) nestedLoot).biologydictionary$getValue();
    }

    public static List<LootPoolEntryContainer> getChildren(CompositeEntryBase composite) {
        return ((CompositeEntryBaseIMixin) composite).biologydictionary$getChildren();
    }

    private static List<LootItemCondition> conditionValues(Optional<Holder<LootItemCondition>> condition) {
        return condition.map(holder -> flattenCondition(holder.value())).orElseGet(List::of);
    }

    private static List<LootItemCondition> flattenCondition(LootItemCondition condition) {
        if (condition instanceof AllOfCondition) {
            HolderSet<LootItemCondition> terms = ((CompositeLootItemConditionIMixin) condition).biologydictionary$getTerms();
            List<LootItemCondition> result = new ArrayList<>(terms.size());
            for (Holder<LootItemCondition> term : terms) {
                result.addAll(flattenCondition(term.value()));
            }
            return result;
        }
        return List.of(condition);
    }

    public static List<LootItemCondition> getConditions(LootPool pool) {
        return conditionValues(((LootPoolIMixin) pool).biologydictionary$getCondition());
    }

    public static List<LootItemCondition> getConditions(LootPoolEntryContainer entryContainer) {
        return conditionValues(((LootPoolEntryContainerIMixin) entryContainer).biologydictionary$getCondition());
    }

    public static Identifier getConditionType(LootItemCondition condition) {
        return BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.codec());
    }

    public static Holder<ContextIntProvider> getCountValue(SetItemCountFunction function) {
        return ((SetItemCountFunctionIMixin) function).biologydictionary$getValue();
    }

    /**
     * Parse loot table entries with probability information.
     * Only handles most common cases. Some complex situations and parts that depend on dynamic information
     * are not processed, so this probability is not completely accurate.
     * <p>
     * Returns a list of LootEntry objects containing item, count range, and drop chance.
     */
    public static List<LootEntry> parseLootEntries(LootTable lootTable) {
        return parseLootEntries(lootTable, 0);
    }

    private static List<LootEntry> parseLootEntries(LootTable lootTable, int initialDepth) {
        List<LootEntry> entries = new ArrayList<>();
        List<LootPool> pools = getPools(lootTable);

        for (LootPool pool : pools) {
            float totalWeight = calculatePoolWeight(pool);

            // Get pool-level conditions
            List<LootItemCondition> poolConditions = getConditions(pool);
            List<Identifier> poolConditionTypes = poolConditions.stream()
                    .map(LootTableUtils::getConditionType).toList();
            float poolChance = extractConditionChance(poolConditions);

            List<LootPoolEntryContainer> entryContainers = getEntries(pool);
            for (LootPoolEntryContainer entryContainer : entryContainers) {
                // Calculate base chance by weight
                float baseChance = poolChance;
                if (entryContainer instanceof UniformContainerBase singleton) {
                    int weight = getWeight(singleton);
                    baseChance = mulChance(baseChance, totalWeight > 0 ? (weight / totalWeight) : -1F);
                }

                List<LootEntry> parsedEntries = parseLootPoolEntryContainer(
                        entryContainer, baseChance, poolConditionTypes, initialDepth);
                entries.addAll(parsedEntries);
            }
        }

        return entries;
    }

    /**
     * Calculate the total weight of all entries in a pool.
     */
    private static float calculatePoolWeight(LootPool pool) {
        float totalWeight = 0f;
        for (LootPoolEntryContainer entry : getEntries(pool)) {
            if (entry instanceof UniformContainerBase singleton) {
                totalWeight += getWeight(singleton);
            }
        }
        return totalWeight;
    }

    /**
     * Parse a single loot entry container and return LootEntry objects.
     */
    private static List<LootEntry> parseLootPoolEntryContainer(LootPoolEntryContainer entry, float baseChance,
            List<Identifier> inheritedConditions, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return List.of();
        }

        // Get entry-level conditions
        List<LootItemCondition> entryConditions = getConditions(entry);
        List<Identifier> entryConditionTypes = entryConditions.stream()
                .map(LootTableUtils::getConditionType).toList();

        // Combine inherited and entry-level conditions
        List<Identifier> allConditions = new ArrayList<>(inheritedConditions);
        allConditions.addAll(entryConditionTypes);
        float conditionMultiplier = extractConditionChance(getConditions(entry));
        float dropChance = mulChance(baseChance, conditionMultiplier);

        if (entry instanceof UniformContainerBase singleton) {
            int[] countRange = extractCountRange(singleton);
            int minCount = countRange[0];
            int maxCount = countRange[1];
            if (minCount <= 0) {
                dropChance = mulChance(dropChance, ((float) maxCount) / (1F + maxCount - minCount));
                minCount = 1;
            }

            return switch (singleton) {
                case LootItem lootItem -> List.of(
                        parseLootItem(lootItem, minCount, maxCount, dropChance, allConditions));
                case TagEntry tagEntry -> parseTagEntry(tagEntry, minCount, maxCount, dropChance, allConditions);
                case NestedLootTable nestedLoot -> parseNestedLootTable(nestedLoot, dropChance, allConditions, depth);
                default -> List.of();
            };
        } else if (entry instanceof CompositeEntryBase composite) {
            return parseCompositeEntry(composite, dropChance, allConditions, depth);
        }
        return List.of();
    }


    /**
     * Extract chance multiplier from conditions.
     * Looks for random_chance or random_chance_with_enchanted_bonus conditions.
     * Returns 1.0 if no such condition is found (no multiplier).
     */
    private static float extractConditionChance(List<LootItemCondition> conditions) {
        for (LootItemCondition condition : conditions) {
            if (condition instanceof LootItemRandomChanceCondition c) {
                if (c.chance().value() instanceof net.minecraft.world.level.storage.loot.providers.number.floats.ConstantValue(float value)) {
                    return value;
                }
                return -1F;
            } else if (condition instanceof LootItemRandomChanceWithEnchantedBonusCondition c) {
                return c.unenchantedChance();
            } else if (condition instanceof BonusLevelTableCondition c) {
                return c.values().isEmpty() ? 0 : c.values().getFirst();
            }
        }
        return 1.0f;
    }

    /**
     * Parse a LootItem entry (single item).
     */
    private static LootEntry parseLootItem(LootItem lootItem, int minCount, int maxCount, float dropChance,
                                           List<Identifier> conditions) {
        Holder<Item> itemHolder = getItem(lootItem);
        return new LootEntry(itemHolder.value(), minCount, maxCount, dropChance, conditions);
    }

    /**
     * Parse a TagEntry (all items in a tag).
     */
    private static List<LootEntry> parseTagEntry(TagEntry tagEntry, int minCount, int maxCount, float dropChance,
                                                 List<Identifier> conditions) {
        HolderSet<Item> tag = getTag(tagEntry);

        List<LootEntry> entries = new ArrayList<>();
        if (tag.size() == 0) {
            return entries;
        }

        float chance = dropChance / tag.size();
        for (Holder<Item> holder : tag) {
            entries.add(new LootEntry(holder.value(), minCount, maxCount, chance, conditions));
        }

        return entries;
    }

    /**
     * Parse a NestedLootTable entry.
     * Only processes inline tables, skips references.
     */
    private static List<LootEntry> parseNestedLootTable(NestedLootTable nestedLoot, float dropChance,
                                                        List<Identifier> conditions, int depth) {
        HolderSet<LootTable> contents = getContents(nestedLoot);
        List<LootEntry> result = new ArrayList<>();

        for (Holder<LootTable> holder : contents) {
            LootTable inlineTable = holder.unwrap().right().orElse(null);
            if (inlineTable == null) {
                // Reference to another loot table; keep the existing behavior of skipping it.
                continue;
            }

            result.addAll(parseLootEntries(inlineTable, depth + 1).stream()
                    .map(entry -> {
                        List<Identifier> combinedConditions = new ArrayList<>(conditions);
                        combinedConditions.addAll(entry.conditions());
                        return new LootEntry(
                                entry.item(),
                                entry.minCount(),
                                entry.maxCount(),
                                mulChance(entry.dropChance(), dropChance),
                                combinedConditions
                        );
                    })
                    .toList());
        }

        return result;
    }

    /**
     * Parse a CompositeEntryBase (EntryGroup, AlternativesEntry, SequentialEntry).
     */
    private static List<LootEntry> parseCompositeEntry(CompositeEntryBase composite, float dropChance,
                                                       List<Identifier> conditions, int depth) {
        List<LootPoolEntryContainer> children = getChildren(composite);
        List<LootEntry> entries = new ArrayList<>();

        for (LootPoolEntryContainer child : children) {
            entries.addAll(parseLootPoolEntryContainer(child, dropChance, conditions, depth + 1));
        }

        return entries;
    }

    /**
     * Extract min and max count from a UniformContainerBase's functions.
     * Returns int array [minCount, maxCount].
     */
    private static int[] extractCountRange(UniformContainerBase singleton) {
        int minCount = 1;
        int maxCount = 1;

        List<LootItemFunction> functions = getFunctions(singleton);
        for (LootItemFunction function : functions) {
            if (function instanceof SetItemCountFunction setCount) {
                Holder<ContextIntProvider> value = getCountValue(setCount);
                int[] range = extractRangeFromNumberProvider(value);
                if (range != null) {
                    minCount = range[0];
                    maxCount = range[1];
                }
                break; // Only use the first SetItemCountFunction
            }
        }

        return new int[]{minCount, maxCount};
    }

    /**
     * Extract min and max from a number provider.
     * Returns int array [min, max] or null if cannot be determined.
     */
    private static int[] extractRangeFromNumberProvider(Holder<ContextIntProvider> provider) {
        ContextIntProvider value = provider.value();
        if (value instanceof net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue(int constant)) {
            return new int[]{constant, constant};
        } else if (value instanceof UniformGenerator uniform) {
            int[] minRange = extractRangeFromNumberProvider(uniform.min());
            int[] maxRange = extractRangeFromNumberProvider(uniform.max());
            if (minRange != null && maxRange != null) {
                return new int[]{minRange[0], maxRange[1]};
            }
        }
        return null; // Cannot determine range for other providers (e.g., BinomialDistributionGenerator)
    }

    private static float mulChance(float a, float b) {
        if (a < 0 || b < 0) { return -1; }
        return a * b;
    }

    public record LootEntry(Item item, int minCount, int maxCount, float dropChance, List<Identifier> conditions) {

        public static LootEntry fromNbt(CompoundTag nbt) {
            String itemId = NbtUtils.getString(nbt, "item");
            Item item = BuiltInRegistries.ITEM.get(IdentifierUtils.fromString(itemId)).map(Holder.Reference::value).orElseThrow();
            int minCount = NbtUtils.getInt(nbt, "minCount");
            int maxCount = NbtUtils.getInt(nbt, "maxCount");
            float dropChance = NbtUtils.getFloat(nbt, "dropChance");

            // Parse conditions
            List<Identifier> conditions = new ArrayList<>();
            if (NbtUtils.contains(nbt, "conditions")) {
                ListTag conditionsList = NbtUtils.getListOr(nbt, "conditions", new ListTag());
                for (Tag tag : conditionsList) {
                    if (tag instanceof StringTag conditionId) {
                        conditions.add(IdentifierUtils.fromString(conditionId.asString().orElseThrow()));
                    }
                }
            }

            return new LootEntry(item, minCount, maxCount, dropChance, conditions);
        }

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            NbtUtils.putString(nbt, "item", IdentifierUtils.toString(itemId));
            NbtUtils.putInt(nbt, "minCount", minCount);
            NbtUtils.putInt(nbt, "maxCount", maxCount);
            NbtUtils.putFloat(nbt, "dropChance", dropChance);

            // Save conditions
            ListTag conditionsList = new ListTag();
            for (Identifier condition : conditions) {
                conditionsList.add(IdentifierUtils.toNbt(condition));
            }
            NbtUtils.put(nbt, "conditions", conditionsList);

            return nbt;
        }

        public Component getDisplayName() {
            return item.getName(new ItemStack(item));
        }
    }
}

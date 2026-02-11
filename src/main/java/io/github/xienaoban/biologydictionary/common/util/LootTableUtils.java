package io.github.xienaoban.biologydictionary.common.util;

import com.mojang.datafixers.util.Either;
import io.github.xienaoban.biologydictionary.mixin.*;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.*;


public final class LootTableUtils {
    private static final int MAX_RECURSION_DEPTH = 2;

    public static boolean hasLootTable(Entity entity) {
        return getLootTableKey(entity).isPresent();
    }

    public static Optional<ResourceKey<LootTable>> getLootTableKey(Entity entity) {
        return entity.getLootTable();
    }

    public static List<LootPool> getPools(LootTable lootTable) {
        return ((LootTableIMixin) lootTable).getPools();
    }

    public static List<LootPoolEntryContainer> getEntries(LootPool pool) {
        return ((LootPoolIMixin) pool).getEntries();
    }

    public static Holder<Item> getItem(LootItem lootItem) {
        return ((LootItemIMixin) lootItem).getItem();
    }

    public static int getWeight(LootPoolSingletonContainer singleton) {
        return ((LootPoolSingletonContainerIMixin) singleton).getWeight();
    }

    public static List<LootItemFunction> getFunctions(LootPoolSingletonContainer singleton) {
        return ((LootPoolSingletonContainerIMixin) singleton).getFunctions();
    }

    public static TagKey<Item> getTag(TagEntry tagEntry) {
        return ((TagEntryIMixin) tagEntry).getTag();
    }

    public static Either<ResourceKey<LootTable>, LootTable> getContents(NestedLootTable nestedLoot) {
        return ((NestedLootTableIMixin) nestedLoot).getContents();
    }

    public static List<LootPoolEntryContainer> getChildren(CompositeEntryBase composite) {
        return ((CompositeEntryBaseIMixin) composite).getChildren();
    }
    
    public static List<LootItemCondition> getConditions(LootPool pool) {
        return ((LootPoolIMixin) pool).getConditions();
    }
    
    public static List<LootItemCondition> getConditions(LootPoolEntryContainer entryContainer) {
        return ((LootPoolEntryContainerIMixin) entryContainer).getConditions();
    }
    
    public static Identifier getConditionType(LootItemCondition condition) {
        return BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());
    }

    public static NumberProvider getCountValue(SetItemCountFunction function) {
        return ((SetItemCountFunctionIMixin) function).getValue();
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
            if (totalWeight <= 0) continue;

            // Get pool-level conditions
            List<LootItemCondition> poolConditions = getConditions(pool);
            List<Identifier> poolConditionTypes = poolConditions.stream()
                    .map(LootTableUtils::getConditionType).toList();

            List<LootPoolEntryContainer> entryContainers = getEntries(pool);
            for (LootPoolEntryContainer entryContainer : entryContainers) {
                List<LootEntry> parsedEntries = parseEntry(entryContainer, totalWeight, initialDepth, poolConditionTypes);
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
            if (entry instanceof LootPoolSingletonContainer singleton) {
                totalWeight += getWeight(singleton);
            }
        }
        return totalWeight;
    }

    /**
     * Parse a single loot entry container and return LootEntry objects.
     */
    private static List<LootEntry> parseEntry(LootPoolEntryContainer entry, float totalWeight, int depth, List<Identifier> inheritedConditions) {
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

        return switch (entry) {
            case LootItem lootItem -> List.of(parseLootItem(lootItem, totalWeight, allConditions));
            case TagEntry tagEntry -> parseTagEntry(tagEntry, totalWeight, allConditions);
            case NestedLootTable nestedLoot -> parseNestedLootTable(nestedLoot, depth, allConditions);
            case CompositeEntryBase composite -> parseCompositeEntry(composite, totalWeight, depth, allConditions);
            default -> List.of();
        };
    }

    /**
     * Parse a LootItem entry (single item).
     */
    private static LootEntry parseLootItem(LootItem lootItem, float totalWeight, List<Identifier> conditions) {
        Holder<Item> itemHolder = getItem(lootItem);
        int weight = getWeight(lootItem);

        int[] countRange = extractCountRange(lootItem);
        int minCount = countRange[0];
        int maxCount = countRange[1];
        float dropChance = totalWeight > 0 ? (weight / totalWeight) : 0f;

        return new LootEntry(itemHolder.value(), minCount, maxCount, dropChance, conditions);
    }

    /**
     * Parse a TagEntry (all items in a tag).
     */
    private static List<LootEntry> parseTagEntry(TagEntry tagEntry, float totalWeight, List<Identifier> conditions) {
        var tag = getTag(tagEntry);
        int weight = getWeight(tagEntry);

        int[] countRange = extractCountRange(tagEntry);
        int minCount = countRange[0];
        int maxCount = countRange[1];
        float dropChance = totalWeight > 0 ? (weight / totalWeight) : 0f;

        List<LootEntry> entries = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
            if (holder.is(tag)) {
                entries.add(new LootEntry(item, minCount, maxCount, dropChance, conditions));
            }
        }
        return entries;
    }

    /**
     * Parse a NestedLootTable entry.
     * Only processes inline tables, skips references.
     */
    private static List<LootEntry> parseNestedLootTable(NestedLootTable nestedLoot, int depth, List<Identifier> conditions) {
        Either<ResourceKey<LootTable>, LootTable> contents = getContents(nestedLoot);

        if (contents.right().isPresent()) {
            // Inline loot table - process recursively with increased depth
            LootTable inlineTable = contents.right().get();
            
            // Create a wrapper function to apply inherited conditions to all entries from the nested table
            return parseLootEntries(inlineTable, depth + 1).stream()
                .map(entry -> {
                    // Combine inherited conditions with nested entry conditions
                    List<Identifier> combinedConditions = new ArrayList<>(conditions);
                    combinedConditions.addAll(entry.conditions());
                    return new LootEntry(
                        entry.item(),
                        entry.minCount(),
                        entry.maxCount(),
                        entry.dropChance(),
                        combinedConditions
                    );
                })
                .toList();
        }

        return List.of();
    }

    /**
     * Parse a CompositeEntryBase (EntryGroup, AlternativesEntry, SequentialEntry).
     */
    private static List<LootEntry> parseCompositeEntry(CompositeEntryBase composite, float totalWeight, int depth, List<Identifier> conditions) {
        List<LootPoolEntryContainer> children = getChildren(composite);
        List<LootEntry> entries = new ArrayList<>();

        for (LootPoolEntryContainer child : children) {
            entries.addAll(parseEntry(child, totalWeight, depth + 1, conditions));
        }

        return entries;
    }

    /**
     * Extract min and max count from a LootPoolSingletonContainer's functions.
     * Returns int array [minCount, maxCount].
     */
    private static int[] extractCountRange(LootPoolSingletonContainer singleton) {
        int minCount = 1;
        int maxCount = 1;

        List<LootItemFunction> functions = getFunctions(singleton);
        for (LootItemFunction function : functions) {
            if (function instanceof SetItemCountFunction setCount) {
                NumberProvider value = getCountValue(setCount);
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
     * Extract min and max from a NumberProvider.
     * Returns int array [min, max] or null if cannot be determined.
     */
    private static int[] extractRangeFromNumberProvider(NumberProvider provider) {
        if (provider instanceof ConstantValue(float value1)) {
            int value = (int) value1;
            return new int[]{value, value};
        } else if (provider instanceof UniformGenerator(NumberProvider min, NumberProvider max)) {
            int[] minRange = extractRangeFromNumberProvider(min);
            int[] maxRange = extractRangeFromNumberProvider(max);
            if (minRange != null && maxRange != null) {
                return new int[]{minRange[0], maxRange[1]};
            }
        }
        return null; // Cannot determine range for other providers (e.g., BinomialDistributionGenerator)
    }

    public record LootEntry(Item item, int minCount, int maxCount, float dropChance, List<Identifier> conditions) {

        public static LootEntry fromNbt(CompoundTag nbt) {
            String itemId = nbt.getString("item").orElseThrow();
            Item item = BuiltInRegistries.ITEM.get(Identifier.parse(itemId)).map(Holder.Reference::value).orElseThrow();
            int minCount = nbt.getInt("minCount").orElseThrow();
            int maxCount = nbt.getInt("maxCount").orElseThrow();
            float dropChance = nbt.getFloat("dropChance").orElseThrow();
            
            // Parse conditions
            List<Identifier> conditions = new ArrayList<>();
            if (nbt.contains("conditions")) {
                ListTag conditionsList = nbt.getList("conditions").orElse(new ListTag());
                for (Tag tag : conditionsList) {
                    if (tag instanceof StringTag conditionId) {
                        conditions.add(Identifier.parse(conditionId.asString().orElseThrow()));
                    }
                }
            }
            
            return new LootEntry(item, minCount, maxCount, dropChance, conditions);
        }

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
            nbt.putString("item", itemId.toString());
            nbt.putInt("minCount", minCount);
            nbt.putInt("maxCount", maxCount);
            nbt.putFloat("dropChance", dropChance);
            
            // Save conditions
            ListTag conditionsList = new ListTag();
            for (Identifier condition : conditions) {
                conditionsList.add(StringTag.valueOf(condition.toString()));
            }
            nbt.put("conditions", conditionsList);
            
            return nbt;
        }

        public Component getDisplayName() {
            return item.getName();
        }
    }
}
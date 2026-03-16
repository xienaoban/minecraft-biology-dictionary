package io.github.xienaoban.biologydictionary.platform.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.xienaoban.biologydictionary.mixin.loot.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class LootTableUtils {
    private static final int MAX_RECURSION_DEPTH = 3;

    public static boolean hasLootTable(LivingEntity entity) {
        return getLootTableKey(entity) != null;
    }

    public static ResourceLocation getLootTableKey(LivingEntity entity) {
        return entity.getLootTable();
    }

    @ExpectPlatform
    public static LootPool[] getPools(LootTable lootTable) {
        throw new AssertionError();
    }

    public static LootPoolEntryContainer[] getEntries(LootPool pool) {
        return ((LootPoolIMixin) pool).biologydictionary$getEntries();
    }

    public static Item getItem(LootItem lootItem) {
        return ((LootItemIMixin) lootItem).biologydictionary$getItem();
    }

    public static int getWeight(LootPoolSingletonContainer singleton) {
        return ((LootPoolSingletonContainerIMixin) singleton).biologydictionary$getWeight();
    }

    public static LootItemFunction[] getFunctions(LootPoolSingletonContainer singleton) {
        return ((LootPoolSingletonContainerIMixin) singleton).biologydictionary$getFunctions();
    }

    public static TagKey<Item> getTag(TagEntry tagEntry) {
        return ((TagEntryIMixin) tagEntry).biologydictionary$getTag();
    }

    public static ResourceLocation getNestedLootTableName(LootTableReference nestedLoot) {
        return ((NestedLootTableIMixin) nestedLoot).biologydictionary$getName();
    }

    public static LootPoolEntryContainer[] getChildren(CompositeEntryBase composite) {
        return ((CompositeEntryBaseIMixin) composite).biologydictionary$getChildren();
    }

    public static LootItemCondition[] getConditions(LootPool pool) {
        return ((LootPoolIMixin) pool).biologydictionary$getConditions();
    }

    public static LootItemCondition[] getConditions(LootPoolEntryContainer entryContainer) {
        return ((LootPoolEntryContainerIMixin) entryContainer).biologydictionary$getConditions();
    }

    public static ResourceLocation getConditionType(LootItemCondition condition) {
        return BuiltInRegistries.LOOT_CONDITION_TYPE.getKey(condition.getType());
    }

    public static NumberProvider getCountValue(SetItemCountFunction function) {
        return ((SetItemCountFunctionIMixin) function).biologydictionary$getValue();
    }

    public static float getProbability(LootItemRandomChanceCondition condition) {
        return ((LootItemRandomChanceConditionIMixin) condition).biologydictionary$getProbability();
    }

    public static float getPercent(LootItemRandomChanceWithLootingCondition condition) {
        return ((LootItemRandomChanceWithLootingConditionIMixin) condition).biologydictionary$getPercent();
    }

    public static float[] getValues(BonusLevelTableCondition condition) {
        return ((BonusLevelTableConditionIMixin) condition).biologydictionary$getValues();
    }

    public static float getValue(ConstantValue constantValue) {
        return ((ConstantValueIMixin) (Object) constantValue).biologydictionary$getValue();
    }

    public static NumberProvider getMin(UniformGenerator uniformGenerator) {
        return ((UniformGeneratorIMixin) uniformGenerator).biologydictionary$getMin();
    }

    public static NumberProvider getMax(UniformGenerator uniformGenerator) {
        return ((UniformGeneratorIMixin) uniformGenerator).biologydictionary$getMax();
    }

    /**
     * Parse loot table entries with probability information.
     * Only handles most common cases. Some complex situations and parts that depend on dynamic information
     * are not processed, so this probability is not completely accurate.
     * <p>
     * Returns a list of LootEntry objects containing item, count range, and drop chance.
     */
    public static List<LootEntry> parseLootEntries(LootTable lootTable, LivingEntity entity) {
        return parseLootEntries(lootTable, entity, 0);
    }

    private static List<LootEntry> parseLootEntries(LootTable lootTable, LivingEntity entity, int initialDepth) {
        List<LootEntry> entries = new ArrayList<>();
        LootPool[] pools = getPools(lootTable);

        for (LootPool pool : pools) {
            float totalWeight = calculatePoolWeight(pool);

            // Get pool-level conditions
            LootItemCondition[] poolConditions = getConditions(pool);
            List<ResourceLocation> poolConditionTypes = Arrays.stream(poolConditions)
                    .map(LootTableUtils::getConditionType).toList();
            float poolChance = extractConditionChance(poolConditions);

            LootPoolEntryContainer[] entryContainers = getEntries(pool);
            for (LootPoolEntryContainer entryContainer : entryContainers) {
                // Calculate base chance by weight
                float baseChance = poolChance;
                if (entryContainer instanceof LootPoolSingletonContainer singleton) {
                    int weight = getWeight(singleton);
                    baseChance = mulChance(baseChance, totalWeight > 0 ? (weight / totalWeight) : -1F);
                }

                List<LootEntry> parsedEntries = parseLootPoolEntryContainer(
                        entryContainer, entity, baseChance, poolConditionTypes, initialDepth);
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
    private static List<LootEntry> parseLootPoolEntryContainer(LootPoolEntryContainer entry, LivingEntity entity, float baseChance, List<ResourceLocation> inheritedConditions, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return List.of();
        }

        // Get entry-level conditions
        LootItemCondition[] entryConditions = getConditions(entry);
        List<ResourceLocation> entryConditionTypes = Arrays.stream(entryConditions)
                .map(LootTableUtils::getConditionType).toList();

        // Combine inherited and entry-level conditions
        List<ResourceLocation> allConditions = new ArrayList<>(inheritedConditions);
        allConditions.addAll(entryConditionTypes);
        float conditionMultiplier = extractConditionChance(getConditions(entry));
        float dropChance = mulChance(baseChance, conditionMultiplier);

        if (entry instanceof LootPoolSingletonContainer singleton) {
            int[] countRange = extractCountRange(singleton);
            int minCount = countRange[0];
            int maxCount = countRange[1];
            if (minCount <= 0) {
                dropChance = mulChance(dropChance, ((float) maxCount) / (1F + maxCount - minCount));
                minCount = 1;
            }

            if (singleton instanceof LootItem lootItem) {
                return List.of(parseLootItem(lootItem, minCount, maxCount, dropChance, allConditions));
            } else if (singleton instanceof TagEntry tagEntry) {
                return parseTagEntry(tagEntry, minCount, maxCount, dropChance, allConditions);
            } else if (singleton instanceof LootTableReference lootTableReference) {
                return parseNestedLootTable(lootTableReference, dropChance, allConditions, entity, depth);
            }
            return List.of();
        } else if (entry instanceof CompositeEntryBase composite) {
            return parseCompositeEntry(composite, entity, dropChance, allConditions, depth);
        }
        return List.of();
    }


    /**
     * Extract chance multiplier from conditions.
     * Looks for random_chance or random_chance_with_enchanted_bonus conditions.
     * Returns 1.0 if no such condition is found (no multiplier).
     */
    private static float extractConditionChance(LootItemCondition[] conditions) {
        for (LootItemCondition condition : conditions) {
            if (condition instanceof LootItemRandomChanceCondition c) {
                return getProbability(c);
            } else if (condition instanceof LootItemRandomChanceWithLootingCondition c) {
                return getPercent(c);
            } else if (condition instanceof BonusLevelTableCondition c) {
                float[] values = getValues(c);
                return values.length == 0 ? 0 : values[0];
            }
        }
        return 1.0f;
    }

    /**
     * Parse a LootItem entry (single item).
     */
    private static LootEntry parseLootItem(LootItem lootItem, int minCount, int maxCount, float dropChance,
                                           List<ResourceLocation> conditions) {
        Item item = getItem(lootItem);
        return new LootEntry(item, minCount, maxCount, dropChance, conditions);
    }

    /**
     * Parse a TagEntry (all items in a tag).
     */
    private static List<LootEntry> parseTagEntry(TagEntry tagEntry, int minCount, int maxCount, float dropChance,
                                                 List<ResourceLocation> conditions) {
        TagKey<Item> tag = getTag(tagEntry);

        List<LootEntry> entries = new ArrayList<>();

        int[] sizeObj = new int[1];
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(ignored -> ++sizeObj[0]);
        int size = sizeObj[0];
        float chance = dropChance / size;
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder
                -> entries.add(new LootEntry(holder.value(), minCount, maxCount, chance, conditions)));

        return entries;
    }

    /**
     * Parse a LootTableReference entry (nested loot table).
     * Fetches the referenced table from the server's loot data manager and parses it recursively.
     */
    private static List<LootEntry> parseNestedLootTable(LootTableReference lootTableReference, float dropChance,
                                                        List<ResourceLocation> conditions, LivingEntity entity, int depth) {
        ResourceLocation tableName = getNestedLootTableName(lootTableReference);

        // Get the referenced loot table from server
        LootTable referencedTable = entity.level().getServer().getLootData().getLootTable(tableName);

        // Create a wrapper function to apply inherited conditions to all entries from the referenced table
        return parseLootEntries(referencedTable, entity, depth + 1).stream()
                .map(entry -> {
                    // Combine inherited conditions with referenced entry conditions
                    List<ResourceLocation> combinedConditions = new ArrayList<>(conditions);
                    combinedConditions.addAll(entry.conditions());
                    return new LootEntry(
                            entry.item(),
                            entry.minCount(),
                            entry.maxCount(),
                            mulChance(entry.dropChance(), dropChance),
                            combinedConditions
                    );
                })
                .toList();
    }

    /**
     * Parse a CompositeEntryBase (EntryGroup, AlternativesEntry, SequentialEntry).
     */
    private static List<LootEntry> parseCompositeEntry(CompositeEntryBase composite, LivingEntity entity, float dropChance,
                                                       List<ResourceLocation> conditions, int depth) {
        LootPoolEntryContainer[] children = getChildren(composite);
        List<LootEntry> entries = new ArrayList<>();

        for (LootPoolEntryContainer child : children) {
            entries.addAll(parseLootPoolEntryContainer(child, entity, dropChance, conditions, depth + 1));
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

        LootItemFunction[] functions = getFunctions(singleton);
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
        if (provider instanceof ConstantValue constantValue) {
            int value = (int) getValue(constantValue);
            return new int[]{value, value};
        } else if (provider instanceof UniformGenerator uniformGenerator) {
            NumberProvider min = getMin(uniformGenerator);
            NumberProvider max = getMax(uniformGenerator);
            int[] minRange = extractRangeFromNumberProvider(min);
            int[] maxRange = extractRangeFromNumberProvider(max);
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

    public record LootEntry(Item item, int minCount, int maxCount, float dropChance, List<ResourceLocation> conditions) {

        public static LootEntry fromNbt(CompoundTag nbt) {
            String itemId = nbt.getString("item");
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemId));
            int minCount = nbt.getInt("minCount");
            int maxCount = nbt.getInt("maxCount");
            float dropChance = nbt.getFloat("dropChance");

            // Parse conditions
            List<ResourceLocation> conditions = new ArrayList<>();
            if (nbt.contains("conditions")) {
                ListTag conditionsList = nbt.getList("conditions", Tag.TAG_STRING);
                for (Tag tag : conditionsList) {
                    if (tag instanceof StringTag conditionId) {
                        conditions.add(ResourceLocation.tryParse(conditionId.getAsString()));
                    }
                }
            }

            return new LootEntry(item, minCount, maxCount, dropChance, conditions);
        }

        public CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            nbt.putString("item", itemId.toString());
            nbt.putInt("minCount", minCount);
            nbt.putInt("maxCount", maxCount);
            nbt.putFloat("dropChance", dropChance);

            // Save conditions
            ListTag conditionsList = new ListTag();
            for (ResourceLocation condition : conditions) {
                conditionsList.add(StringTag.valueOf(condition.toString()));
            }
            nbt.put("conditions", conditionsList);

            return nbt;
        }

        public Component getDisplayName() {
            return item.getDescription();
        }
    }
}

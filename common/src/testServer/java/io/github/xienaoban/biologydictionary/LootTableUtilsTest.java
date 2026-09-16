package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.platform.util.LootTableUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LootTableUtilsTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testParseAllBuiltInLootTables(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        if (server == null) {
            helper.fail("Server is not available");
            return;
        }

        List<String> failures = new ArrayList<>();
        int nonEmptyTables = 0;
        int parsedEntries = 0;

        for (ResourceKey<LootTable> key : BuiltInLootTables.all()) {
            try {
                LootTable lootTable = server.reloadableRegistries().getLootTable(key);
                List<LootTableUtils.LootEntry> entries = LootTableUtils.parseLootEntries(lootTable);

                for (LootTableUtils.LootEntry entry : entries) {
                    if (entry.item() == null) {
                        throw new AssertionError("Loot entry has null item");
                    }
                    if (entry.minCount() < 1 || entry.maxCount() < entry.minCount()) {
                        throw new AssertionError("Invalid count range: " + entry.minCount() + ".." + entry.maxCount());
                    }
                    if (Float.isNaN(entry.dropChance())) {
                        throw new AssertionError("Drop chance is NaN");
                    }
                    if (entry.conditions() == null) {
                        throw new AssertionError("Conditions are null");
                    }
                }

                if (!entries.isEmpty()) {
                    nonEmptyTables++;
                }
                parsedEntries += entries.size();
            } catch (Throwable throwable) {
                failures.add(key + ": " + throwable);
            }
        }

        LOGGER.info("Parsed {} non-empty built-in loot tables, {} entries total", nonEmptyTables, parsedEntries);

        if (!failures.isEmpty()) {
            helper.fail("Failed to parse " + failures.size() + " built-in loot tables: " + failures.stream().limit(10).toList());
            return;
        }
        if (nonEmptyTables == 0) {
            helper.fail("No non-empty built-in loot tables were parsed");
            return;
        }

        assertLootTableContainsItems(helper, server, EntityTypes.ZOMBIE.getDefaultLootTable().orElseThrow(),
                Items.ROTTEN_FLESH);
        assertLootTableContainsItems(helper, server, EntityTypes.COW.getDefaultLootTable().orElseThrow(),
                Items.BEEF, Items.LEATHER);
        assertLootTableHasEntries(helper, server, BuiltInLootTables.SIMPLE_DUNGEON, 1);
        assertLootTableHasEntries(helper, server, BuiltInLootTables.ABANDONED_MINESHAFT, 1);

        helper.succeed();
    }

    private void assertLootTableContainsItems(
            GameTestHelper helper, MinecraftServer server, ResourceKey<LootTable> key, Item... expectedItems) {
        List<LootTableUtils.LootEntry> entries = LootTableUtils.parseLootEntries(
                server.reloadableRegistries().getLootTable(key));
        for (Item expectedItem : expectedItems) {
            helper.assertTrue(entries.stream().anyMatch(entry -> entry.item() == expectedItem),
                    "Loot table " + key + " should contain " + expectedItem);
        }
    }

    private void assertLootTableHasEntries(
            GameTestHelper helper, MinecraftServer server, ResourceKey<LootTable> key, int minEntries) {
        List<LootTableUtils.LootEntry> entries = LootTableUtils.parseLootEntries(
                server.reloadableRegistries().getLootTable(key));
        helper.assertTrue(entries.size() >= minEntries,
                "Loot table " + key + " should have at least " + minEntries + " entries, got " + entries.size());
    }
}

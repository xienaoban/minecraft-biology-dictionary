package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.LootTableUtilsTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class LootTableUtilsTestImpl {
    private final LootTableUtilsTest test = new LootTableUtilsTest();

    @GameTest
    public void testParseAllBuiltInLootTables(GameTestHelper helper) {
        test.testParseAllBuiltInLootTables(helper);
    }
}

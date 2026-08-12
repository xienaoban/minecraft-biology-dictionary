package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.PluginLookupTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class PluginLookupTestImpl implements FabricGameTest {
    private final PluginLookupTest test = new PluginLookupTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testPluginSkillRegistered(GameTestHelper helper) {
        test.testPluginSkillRegistered(helper);
    }
}

package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.PluginLookupTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class PluginLookupTestImpl {
    private final PluginLookupTest test = new PluginLookupTest();

    @GameTest
    public void testPluginSkillRegistered(GameTestHelper helper) {
        test.testPluginSkillRegistered(helper);
    }
}

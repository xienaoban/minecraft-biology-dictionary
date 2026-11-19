package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.PlatformLoadTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class PlatformLoadTestImpl {
    private final PlatformLoadTest test = new PlatformLoadTest();

    @GameTest
    public void testDevUtilsBridge(GameTestHelper helper) {
        test.testDevUtilsBridge(helper);
    }
}

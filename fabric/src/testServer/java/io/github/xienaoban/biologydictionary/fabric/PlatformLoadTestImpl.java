package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.PlatformLoadTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class PlatformLoadTestImpl implements FabricGameTest {
    private final PlatformLoadTest test = new PlatformLoadTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testDevUtilsBridge(GameTestHelper helper) {
        test.testDevUtilsBridge(helper);
    }
}

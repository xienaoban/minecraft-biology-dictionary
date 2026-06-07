package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.ClientOnlyCheckTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class ClientOnlyCheckTestImpl implements FabricGameTest {
    private final ClientOnlyCheckTest test = new ClientOnlyCheckTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testClientOnlyCheck(GameTestHelper helper) {
        test.testClientOnlyCheck(helper);
    }
}

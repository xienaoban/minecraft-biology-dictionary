package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.ClientOnlyCheckTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class ClientOnlyCheckTestImpl {
    private final ClientOnlyCheckTest test = new ClientOnlyCheckTest();

    @GameTest
    public void testClientOnlyCheck(GameTestHelper helper) {
        test.testClientOnlyCheck(helper);
    }
}

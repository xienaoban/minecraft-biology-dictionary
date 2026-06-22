package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.RegistrarsTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RegistrarsTestImpl {
    private final RegistrarsTest test = new RegistrarsTest();

    @GameTest
    public void testExtraEntityProperties(GameTestHelper helper) {
        test.testExtraEntityProperties(helper);
    }

    @GameTest
    public void testPacketPayloads(GameTestHelper helper) {
        test.testPacketPayloads(helper);
    }

    @GameTest
    public void testPlayerSkills(GameTestHelper helper) {
        test.testPlayerSkills(helper);
    }
}

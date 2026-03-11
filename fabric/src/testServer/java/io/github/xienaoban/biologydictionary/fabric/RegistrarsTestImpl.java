package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.RegistrarsTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RegistrarsTestImpl implements FabricGameTest {
    private final RegistrarsTest test = new RegistrarsTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testExtraEntityProperties(GameTestHelper helper) {
        test.testExtraEntityProperties(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testPacketPayloads(GameTestHelper helper) {
        test.testPacketPayloads(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testPlayerSkills(GameTestHelper helper) {
        test.testPlayerSkills(helper);
    }
}

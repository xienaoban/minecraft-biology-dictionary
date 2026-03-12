package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityBehaviorTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityBehaviorTestImpl implements FabricGameTest {
    private final VanillaEntityBehaviorTest test = new VanillaEntityBehaviorTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAgeableMobAge(GameTestHelper helper) {
        test.testAgeableMobAge(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAgeableMobForcedAge(GameTestHelper helper) {
        test.testAgeableMobForcedAge(helper);
    }
}

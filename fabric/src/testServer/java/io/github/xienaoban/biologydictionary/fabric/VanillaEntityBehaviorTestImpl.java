package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntityBehaviorTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntityBehaviorTestImpl {
    private final VanillaEntityBehaviorTest test = new VanillaEntityBehaviorTest();

    @GameTest
    public void testAgeableMobAge(GameTestHelper helper) {
        test.testAgeableMobAge(helper);
    }

    @GameTest
    public void testAgeableMobForcedAge(GameTestHelper helper) {
        test.testAgeableMobForcedAge(helper);
    }
}

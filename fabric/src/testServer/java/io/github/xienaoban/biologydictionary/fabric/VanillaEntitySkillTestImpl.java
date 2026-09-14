package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntitySkillTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntitySkillTestImpl {
    private final VanillaEntitySkillTest test = new VanillaEntitySkillTest();

    @GameTest
    public void testSkillNamingConvention(GameTestHelper helper) {
        test.testSkillNamingConvention(helper);
    }

    @GameTest
    public void testSkillFactoryMapping(GameTestHelper helper) {
        test.testSkillFactoryMapping(helper);
    }

    @GameTest
    public void testAllSkills(GameTestHelper helper) {
        test.testAllSkills(helper);
    }
}

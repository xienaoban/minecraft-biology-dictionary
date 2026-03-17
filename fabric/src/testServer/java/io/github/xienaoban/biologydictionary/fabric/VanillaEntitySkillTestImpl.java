package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.VanillaEntitySkillTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class VanillaEntitySkillTestImpl implements FabricGameTest {
    private final VanillaEntitySkillTest test = new VanillaEntitySkillTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testSkillNamingConvention(GameTestHelper helper) {
        test.testSkillNamingConvention(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testSkillFactoryMapping(GameTestHelper helper) {
        test.testSkillFactoryMapping(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllSkills(GameTestHelper helper) {
        test.testAllSkills(helper);
    }
}

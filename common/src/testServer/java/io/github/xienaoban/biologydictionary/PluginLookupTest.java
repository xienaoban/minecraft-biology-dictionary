package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.api.BiologyDictionaryPlugin;
import io.github.xienaoban.biologydictionary.api.BiologySkillsPlugin;
import io.github.xienaoban.biologydictionary.api.BiologySkillsRegistrar;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Asserts that the {@link TestPlugin} (discovered via the entrypoint) was dispatched and its marker
 * skill landed in the skill registry.
 */
public final class PluginLookupTest {
    public void testPluginSkillRegistered(GameTestHelper helper) {
        Class<?> skillClass;
        try {
            skillClass = BiologySkills.getSkillClass(TestPlugin.MARKER_SHORT_NAME);
        } catch (Throwable e) {
            helper.fail("Plugin marker skill not registered (discovery/dispatch failed): " + e);
            return;
        }
        if (skillClass == TestPlugin.MarkerSkill.class) {
            helper.succeed();
        } else {
            helper.fail("Marker skill class mismatch: expected MarkerSkill, got " + skillClass);
        }
    }

    /**
     * Test-only plugin discovered via the {@code "biologydictionary"} entrypoint (declared in the
     * unit-test mod's fabric.mod.json). Registers a marker skill so a game test can assert that
     * third-party plugin discovery, dispatch, and registration actually work end-to-end.
     */
    @BiologyDictionaryPlugin
    public static final class TestPlugin implements BiologySkillsPlugin {
        public static final String MARKER_SHORT_NAME = "bd_test_marker_skill";

        @Override
        public void registerBiologySkills(BiologySkillsRegistrar registrar) {
            registrar.register(MarkerSkill.class, MarkerSkill.META);
        }

        public record MarkerSkill() implements GeneralSkill {
            public static final GeneralSkill.Meta<MarkerSkill> META = new GeneralSkill.Meta<>() {
                @Override
                public MarkerSkill create(FriendlyByteBuf buf) {
                    return new MarkerSkill();
                }

                @Override
                public SkillCost getDefaultCost() {
                    return SkillCost.empty();
                }

                @Override
                public String shortName() {
                    return MARKER_SHORT_NAME;
                }
            };

            @Override
            public void write(FriendlyByteBuf buf) {}

            @Override
            public void serverDo(GeneralSkill.ServerContext ctx) {}
        }
    }
}

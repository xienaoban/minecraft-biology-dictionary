package io.github.xienaoban.biologydictionary.api.plugin;

import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;

/**
 * Plugin for registering skills into Biology Dictionary. Discovered and dispatched exactly once
 * during initialization, before the skill registry freezes.
 */
public interface BiologySkillsPlugin {
    void registerBiologySkills(BiologySkillsPlugin.Registrar registrar);

    interface Registrar {
        <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta);

        <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass,
                                                          EntityTargetedSkill.Meta<T> meta);
    }
}

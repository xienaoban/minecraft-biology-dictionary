package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;

/**
 * Registration handle for the skill registry, passed to {@link BiologySkillsPlugin}.
 */
public interface BiologySkillsRegistrar {
    <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta);

    <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta);
}

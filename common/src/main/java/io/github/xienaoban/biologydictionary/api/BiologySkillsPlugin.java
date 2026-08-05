package io.github.xienaoban.biologydictionary.api;

/**
 * Plugin for registering skills into Biology Dictionary. Discovered and dispatched exactly once
 * during initialization, before the skill registry freezes.
 */
public interface BiologySkillsPlugin {
    void registerBiologySkills(BiologySkillsRegistrar registrar);
}

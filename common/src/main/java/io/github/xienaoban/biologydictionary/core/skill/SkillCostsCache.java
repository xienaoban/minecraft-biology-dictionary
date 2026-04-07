package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache of {@link SkillCost} objects by skill class, derived from {@link Configs.ServerConfigs}.
 */
public final class SkillCostsCache {
    private volatile Map<Class<?>, SkillCost> cache;

    public SkillCostsCache() {
        cache = new HashMap<>();
        update(ConfigsManager.getServer());
    }

    /**
     * Rebuild the cache from the given server configs.
     * Iterates skillCosts and builds a Map&lt;Class&lt;?&gt;, SkillCost&gt;.
     */
    public void update(Configs.ServerConfigs serverConfigs) {
        Map<Class<?>, SkillCost> newCache = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : serverConfigs.getSkillCosts().entrySet()) {
            String shortName = entry.getKey();
            Map<String, Object> costData = entry.getValue();
            Class<?> skillClass = BiologySkills.getSkillClass(shortName);
            newCache.put(skillClass, SkillCost.fromMap(costData));
        }

        cache = newCache;
    }

    public SkillCost getSkillCost(Class<?> skillClass) {
        return cache.get(skillClass);
    }
}

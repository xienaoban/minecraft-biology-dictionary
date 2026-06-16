package io.github.xienaoban.biologydictionary.core.skill;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.config.ConfigsUpdateCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache of {@link SkillCost} objects by skill class, derived from {@link Configs.ServerConfigs}.
 */
public final class SkillCostsCache implements ConfigsUpdateCallback {
    private volatile Map<Class<?>, SkillCost> cache;

    public SkillCostsCache() {
        onConfigsUpdate(ConfigsManager.getClient(), ConfigsManager.getServer());
    }

    @Override
    public void onConfigsUpdate(Configs.ClientConfigs clientConfigs, Configs.ServerConfigs serverConfigs) {
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
        return cache.getOrDefault(skillClass, SkillCost.empty());
    }
}

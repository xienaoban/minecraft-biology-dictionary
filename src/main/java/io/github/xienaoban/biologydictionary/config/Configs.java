package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main configuration data class for Biology Dictionary.
 * Uses annotations for automatic YAML serialization and Cloth Config generation.
 * <p>
 * I'm not going to use @Environment(EnvType.CLIENT) here, or things will get too complicated.
 */
@Config(Lang.CONFIG_TITLE)
public final class Configs {

    /**
     * Client-side configuration options.
     * These settings affect local rendering and behavior.
     */
    public static class ClientConfigs implements PostLoader {
        @ConfigEntry
        float screenScale = 1F;

        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        public ClientConfigs() {
            postLoad();
        }

        public float getScreenScale() {
            return screenScale;
        }

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }

        @Override
        public void postLoad() {}

        public enum FirstPersonShoulderEntityPosition {
            NONE, BOTTOM, SIDES, TOP
        }
    }

    /**
     * Server-side configuration options.
     * These settings are used when running a server or singleplayer.
     */
    public static class ServerConfigs implements PostLoader {
        @ConfigEntry
        boolean bookItemRequired = true;

        @ConfigEntry
        boolean bookItemObtainableFromWanderingTrader = true;

        @ConfigEntry
        boolean inheritSilentFromParents = true;

        /**
         * Skill costs configuration in YAML-friendly format.
         * Maps skill short names to their cost data (from SkillCost.toMap()).
         * Always contains all registered skills after initialization.
         * <p>
         * To configure skill costs, edit the YAML config file directly:
         * <pre>
         * server:
         *   skill_costs:
         *     set_invulnerable:
         *       experience_points: 5
         *     highlight_entities:
         *       experience_levels: 1
         *       items:
         *         - item: minecraft:iron_ingot
         *           count: 1
         * </pre>
         */
        @ConfigEntry
        Map<String, Map<String, Object>> skillCosts = new HashMap<>();

        /**
         * Cache of SkillCost objects by skill class for fast access.
         * Always derived from skillCosts after initialization.
         */
        private transient Map<Class<?>, SkillCost> skillCostsCache;

        public ServerConfigs() {
            postLoad();
        }

        @Override
        public void postLoad() {
            completeSkillCosts();
            rebuildSkillCacheCache();
        }

        public boolean isBookItemRequired() {
            return bookItemRequired;
        }

        public boolean isBookItemObtainableFromWanderingTrader() {
            return bookItemObtainableFromWanderingTrader;
        }

        public boolean isInheritSilentFromParents() {
            return inheritSilentFromParents;
        }

        /**
         * Get skill cost for a given skill class.
         * All skills are guaranteed to be present after initialization.
         */
        public SkillCost getSkillCost(Class<?> skillClass) {
            return skillCostsCache.get(skillClass);
        }

        /**
         * Ensure all registered skills are present in skillCosts.
         * Missing skills are added with their default costs.
         */
        private void completeSkillCosts() {
            Map<String, Map<String, Object>> newCosts = new LinkedHashMap<>();
            BiologySkills.registerBuiltIn(new BiologySkills.Registrar() {
                @Override
                public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                    String shortName = meta.shortName();
                    Map<String, Object> v = skillCosts.get(shortName);
                    newCosts.put(shortName, Objects.requireNonNullElseGet(v, () -> meta.getDefaultCost().toMap()));
                }

                @Override
                public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta) {
                    String shortName = meta.shortName();
                    Map<String, Object> v = skillCosts.get(shortName);
                    newCosts.put(shortName, Objects.requireNonNullElseGet(v, () -> meta.getDefaultCost().toMap()));
                }
            });
            // Reduce the possibility of concurrency issues.
            skillCosts = newCosts;
        }

        /**
         * Rebuild skillCostsDeserialized based on the current skillCosts.
         * Must be called after skillCosts is fully populated.
         */
        private void rebuildSkillCacheCache() {
            Map<Class<?>, SkillCost> cache = new HashMap<>();

            for (Map.Entry<String, Map<String, Object>> entry : skillCosts.entrySet()) {
                String shortName = entry.getKey();
                Map<String, Object> costData = entry.getValue();
                Class<?> skillClass = BiologySkills.getSkillClass(shortName);
                cache.put(skillClass, SkillCost.fromMap(costData));
            }

            skillCostsCache = cache;
        }
    }

    @ConfigCategory(Lang.CONFIG_CATEGORY_CLIENT)
    private final ClientConfigs client = new ClientConfigs();

    @ConfigCategory(Lang.CONFIG_CATEGORY_SERVER)
    private final ServerConfigs server = new ServerConfigs();

    public ClientConfigs getClient() {
        return client;
    }

    public ServerConfigs getServer() {
        return server;
    }

    // ==================== Translation Key Utilities ====================

    public static String getConfigNameTranslationKey(String fieldName) {
        return Lang.CONFIG_ENTRY_PREFIX + fieldName;
    }

    public static String getEnumValueTranslationKey(Enum<?> enumValue) {
        return "enum." + enumValue.getClass().getName().replace('$', '.') + "." + enumValue.name();
    }

    /**
     * Called after configuration is loaded from YAML or deserialized from network.
     * This is the place to perform validation, fill in missing values, or build derived data structures.
     */
    @FunctionalInterface
    public interface PostLoader {
        void postLoad();
    }
}

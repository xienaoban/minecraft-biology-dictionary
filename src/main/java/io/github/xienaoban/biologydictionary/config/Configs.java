package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;

import java.util.Map;

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
    public static class ClientConfigs {
        @ConfigEntry
        float screenScale = 1F;

        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        public float getScreenScale() {
            return screenScale;
        }

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }

        public enum FirstPersonShoulderEntityPosition {
            NONE, BOTTOM, SIDES, TOP
        }
    }

    /**
     * Server-side configuration options.
     * These settings are used when running a server or singleplayer.
     */
    public static class ServerConfigs {
        @ConfigEntry
        boolean bookItemRequired = true;

        @ConfigEntry
        boolean bookItemObtainableFromWanderingTrader = true;

        @ConfigEntry
        boolean inheritSilentFromParents = true;

        /**
         * Complete skill costs configuration.
         * Maps skill class names to their costs.
         * This map always contains all registered skills after config is loaded.
         * Skills with default costs are included alongside any custom configurations.
         * Marked transient to hide from Cloth Config UI.
         * <p>
         * To configure skill costs, edit the YAML config file directly:
         * <pre>
         * server:
         *   skill_costs:
         *     io.github.xienaoban.biologydictionary.core.skill.entity.SheepForceEatGrassSkill:
         *       experience_points: 5
         *     io.github.xienaoban.biologydictionary.core.skill.general.GetSpawnEggSkill:
         *       experience_levels: 1
         *       items:
         *         - item: minecraft:iron_ingot
         *           count: 1
         * </pre>
         */
        transient Map<String, SkillCost> skillCosts = Map.of();

        public boolean isBookItemRequired() {
            return bookItemRequired;
        }

        public boolean isBookItemObtainableFromWanderingTrader() {
            return bookItemObtainableFromWanderingTrader;
        }

        public boolean isInheritSilentFromParents() {
            return inheritSilentFromParents;
        }

        public Map<String, SkillCost> getSkillCosts() {
            return skillCosts;
        }

        public void setSkillCosts(Map<String, SkillCost> skillCosts) {
            this.skillCosts = skillCosts;
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
}

package io.github.xienaoban.biologydictionary.config;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.annotation.Config;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigCategory;
import io.github.xienaoban.biologydictionary.config.annotation.ConfigEntry;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.EntityTargetedSkill;
import io.github.xienaoban.biologydictionary.core.skill.GeneralSkill;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main configuration data class for Biology Dictionary.
 * Uses annotations for automatic YAML serialization and Cloth Config generation.
 * <p>
 * I'm not going to use @Environment(EnvType.CLIENT) for client configs here,
 * or things will get too complicated.
 */
@Config(Lang.CONFIG_TITLE)
public final class Configs {

    /**
     * Client-side configuration options.
     * These settings affect local rendering and behavior.
     */
    public static class ClientConfigs implements PostLoader {
        /**
         * Scale factor for Biology Dictionary GUI screens.
         * Does not affect other mod or vanilla interfaces.
         */
        @ConfigEntry
        float screenScale = 1F;

        /**
         * Position where shoulder entities (e.g., parrots) are rendered in first-person view.
         */
        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        /**
         * Hide the entity description widget when no description is available for the current entity.
         */
        @ConfigEntry
        boolean hideEntityDescriptionWidgetIfNotFound = true;

        // =========================== Getters ============================

        public float getScreenScale() {
            return screenScale;
        }

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }

        public boolean isHideEntityDescriptionWidgetIfNotFound() {
            return hideEntityDescriptionWidgetIfNotFound;
        }

        // ============================= Misc =============================

        public ClientConfigs() {
            postLoad();
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
        /**
         * Whether the Biology Dictionary item must be in the player's inventory
         * to open the GUI via keybind in non-creative mode.
         */
        @ConfigEntry
        boolean bookItemRequired = true;

        /**
         * Whether wandering traders have a chance to offer the Biology Dictionary item for trade.
         */
        @ConfigEntry
        boolean bookItemObtainableFromWanderingTrader = true;

        /**
         * Whether baby entities inherit the "silent" trait when both parents are silent.
         */
        @ConfigEntry
        boolean inheritSilentFromParents = true;

        /**
         * Discovery strategy. Determines how entities are discovered by each player.
         */
        @ConfigEntry
        DiscoveryStrategyMode discoveryStrategy = DiscoveryStrategyMode.VANILLA_KILL;

        /**
         * Whether undiscovered entities are allowed to be viewed in the overview screen.
         * When false, undiscovered entities cannot be clicked in the home screen
         * and server refuses to send NBT data for entity overview.
         */
        @ConfigEntry
        boolean allowOverviewForUndiscoveredEntities = false;

        /**
         * Maximum range (in blocks) for telescope entity discovery.
         */
        @ConfigEntry
        double telescopeRange = 160.0;

        /**
         * Enable discovery by opening entity detail screen.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByDetailScreen = true;

        /**
         * Enable discovery by highlighting an entity.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByHighlight = true;

        /**
         * Enable discovery by observing with telescope.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByTelescope = true;

        /**
         * Enable discovery by killing an entity.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByKill = true;

        /**
         * Enable discovery by interacting with an entity.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByInteract = true;

        /**
         * Enable discovery by being killed by an entity.
         * Only effective if the active strategy supports this method.
         */
        @ConfigEntry
        boolean discoveryByKilledBy = true;

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

        // =========================== Getters ============================

        public boolean isBookItemRequired() {
            return bookItemRequired;
        }

        public boolean isBookItemObtainableFromWanderingTrader() {
            return bookItemObtainableFromWanderingTrader;
        }

        public boolean isInheritSilentFromParents() {
            return inheritSilentFromParents;
        }

        public DiscoveryStrategyMode getDiscoveryStrategy() {
            return discoveryStrategy;
        }

        public boolean isAllowOverviewForUndiscoveredEntities() {
            return allowOverviewForUndiscoveredEntities;
        }

        public double getTelescopeRange() {
            return telescopeRange;
        }

        public boolean isDiscoveryByDetailScreen() {
            return discoveryByDetailScreen;
        }

        public boolean isDiscoveryByHighlight() {
            return discoveryByHighlight;
        }

        public boolean isDiscoveryByTelescope() {
            return discoveryByTelescope;
        }

        public boolean isDiscoveryByKill() {
            return discoveryByKill;
        }

        public boolean isDiscoveryByInteract() {
            return discoveryByInteract;
        }

        public boolean isDiscoveryByKilledBy() {
            return discoveryByKilledBy;
        }

        public Map<String, Map<String, Object>> getSkillCosts() {
            return skillCosts;
        }

        // ============================= Misc =============================

        public ServerConfigs() {
            postLoad();
        }

        @Override
        public void postLoad() {
            completeSkillCosts();
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

        public enum DiscoveryStrategyMode {
            ALWAYS_UNLOCKED,
            VANILLA_KILL,
            BIOLOGY_DICTIONARY
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

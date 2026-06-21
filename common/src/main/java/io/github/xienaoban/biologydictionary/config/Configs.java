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

@Config(Lang.CONFIG_TITLE)
public final class Configs {
    public static class ClientConfigs implements PostLoader {
        @ConfigEntry
        float screenScale = 1F;

        @ConfigEntry
        FirstPersonShoulderEntityPosition firstPersonShoulderEntityPosition = FirstPersonShoulderEntityPosition.BOTTOM;

        @ConfigEntry
        boolean hideEntityDescriptionWidgetIfNotFound = true;

        @ConfigEntry
        boolean demoMode = false;

        public float getScreenScale() {
            return screenScale;
        }

        public FirstPersonShoulderEntityPosition getFirstPersonShoulderEntityPosition() {
            return firstPersonShoulderEntityPosition;
        }

        public boolean isHideEntityDescriptionWidgetIfNotFound() {
            return hideEntityDescriptionWidgetIfNotFound;
        }

        public boolean isDemoMode() {
            return demoMode;
        }

        @Override
        public void postLoad() {}

        public enum FirstPersonShoulderEntityPosition {
            NONE,
            BOTTOM,
            SIDES,
            TOP
        }
    }

    public static class ServerConfigs implements PostLoader {
        @ConfigEntry
        boolean bookItemRequired = true;

        @ConfigEntry
        boolean bookItemObtainableFromWanderingTrader = true;

        @ConfigEntry
        boolean inheritSilentFromParents = true;

        @ConfigEntry
        boolean allowStealingPlayerInventory = false;

        @ConfigEntry
        DiscoveryStrategyMode discoveryStrategy = DiscoveryStrategyMode.VANILLA_KILL;

        @ConfigEntry
        boolean allowOverviewForUndiscoveredEntities = false;

        @ConfigEntry
        int telescopeDiscoveryRange = 160;

        @ConfigEntry
        boolean discoveryByDetailScreen = true;

        @ConfigEntry
        boolean discoveryByHighlight = true;

        @ConfigEntry
        boolean discoveryByTelescope = true;

        @ConfigEntry
        boolean discoveryByInteract = true;

        @ConfigEntry
        boolean discoveryByKill = true;

        @ConfigEntry
        boolean discoveryByKilledBy = true;

        @ConfigEntry
        Map<String, Map<String, Object>> skillCosts = new HashMap<>();

        public boolean isBookItemRequired() {
            return bookItemRequired;
        }

        public boolean isBookItemObtainableFromWanderingTrader() {
            return bookItemObtainableFromWanderingTrader;
        }

        public boolean isInheritSilentFromParents() {
            return inheritSilentFromParents;
        }

        public boolean isAllowStealingPlayerInventory() {
            return allowStealingPlayerInventory;
        }

        public DiscoveryStrategyMode getDiscoveryStrategy() {
            return discoveryStrategy;
        }

        public boolean isAllowOverviewForUndiscoveredEntities() {
            return allowOverviewForUndiscoveredEntities;
        }

        public int getTelescopeDiscoveryRange() {
            return telescopeDiscoveryRange;
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

        public boolean isDiscoveryByInteract() {
            return discoveryByInteract;
        }

        public boolean isDiscoveryByKill() {
            return discoveryByKill;
        }

        public boolean isDiscoveryByKilledBy() {
            return discoveryByKilledBy;
        }

        public Map<String, Map<String, Object>> getSkillCosts() {
            return skillCosts;
        }

        @Override
        public void postLoad() {
            completeSkillCosts();
        }

        private void completeSkillCosts() {
            Map<String, Map<String, Object>> newCosts = new LinkedHashMap<>();
            BiologySkills.registerBuiltIn(new BiologySkills.Registrar() {
                @Override
                public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                    String shortName = meta.shortName();
                    Map<String, Object> value = skillCosts.get(shortName);
                    newCosts.put(shortName, Objects.requireNonNullElseGet(value, () -> meta.getDefaultCost().toMap()));
                }

                @Override
                public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass, EntityTargetedSkill.Meta<T> meta) {
                    String shortName = meta.shortName();
                    Map<String, Object> value = skillCosts.get(shortName);
                    newCosts.put(shortName, Objects.requireNonNullElseGet(value, () -> meta.getDefaultCost().toMap()));
                }
            });
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

    public static String getConfigNameTranslationKey(String fieldName) {
        return Lang.CONFIG_ENTRY_PREFIX + fieldName;
    }

    public static String getEnumValueTranslationKey(Enum<?> enumValue) {
        return "enum." + enumValue.getClass().getName().replace('$', '.') + "." + enumValue.name();
    }

    @FunctionalInterface
    public interface PostLoader {
        void postLoad();
    }
}

package io.github.xienaoban.biologydictionary.core.discovery;

import io.github.xienaoban.biologydictionary.config.Configs;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * How a player discovered an entity.
 * Each value maps to the corresponding {@link DiscoveryEventListener} method.
 */
public enum DiscoverySource {
    ENTITY_DETAIL_SCREEN(DiscoveryEventListener::onEntityDetailScreenOpened,    Configs.ServerConfigs::isDiscoveryByDetailScreen),
    HIGHLIGHT(           DiscoveryEventListener::onEntityHighlighted,           Configs.ServerConfigs::isDiscoveryByHighlight),
    TELESCOPE_OBSERVE(   DiscoveryEventListener::onEntityObservedWithTelescope, Configs.ServerConfigs::isDiscoveryByTelescope),
    INTERACT(            DiscoveryEventListener::onEntityInteracted,            Configs.ServerConfigs::isDiscoveryByInteract),
    KILL(                DiscoveryEventListener::onEntityKilled,                Configs.ServerConfigs::isDiscoveryByKill),
    KILLED_BY(           DiscoveryEventListener::onPlayerKilledBy,              Configs.ServerConfigs::isDiscoveryByKilledBy),
    UNKNOWN((l, p, e) -> false, configs -> false);

    private final Invoker<?> invoker;
    private final Predicate<Configs.ServerConfigs> enabled;

    DiscoverySource(Invoker<?> invoker, Predicate<Configs.ServerConfigs> enabled) {
        this.invoker = invoker;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled.test(ConfigsManager.getServer());
    }

    public <P extends Player> boolean dispatch(DiscoveryEventListener<P> listener, P player, Entity entity) {
        @SuppressWarnings("unchecked")
        boolean res = ((Invoker<P>) invoker).invoke(listener, player, entity);
        return res;
    }

    @FunctionalInterface
    private interface Invoker<P extends Player> {
        boolean invoke(DiscoveryEventListener<P> listener, P player, Entity entity);
    }
}

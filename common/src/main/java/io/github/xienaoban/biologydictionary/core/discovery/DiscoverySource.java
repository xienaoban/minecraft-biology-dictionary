package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * How a player discovered an entity.
 * Each value maps to the corresponding {@link DiscoveryEventListener} method.
 */
public enum DiscoverySource {
    ENTITY_DETAIL_SCREEN(DiscoveryEventListener::onEntityDetailScreenOpened),
    HIGHLIGHT(DiscoveryEventListener::onEntityHighlighted),
    TELESCOPE_OBSERVE(DiscoveryEventListener::onEntityObservedWithTelescope),
    KILL(DiscoveryEventListener::onEntityKilled),
    ATTACK(DiscoveryEventListener::onEntityAttacked),
    INTERACT(DiscoveryEventListener::onEntityInteracted),
    FEED(DiscoveryEventListener::onEntityFed),
    TAME(DiscoveryEventListener::onEntityTamed),
    UNKNOWN(null);

    private final Invoker<?> invoker;

    DiscoverySource(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    public <P extends Player> boolean dispatch(DiscoveryEventListener<P> listener, P player, Entity entity) {
        if (invoker == null) return false;
        @SuppressWarnings("unchecked")
        boolean res = ((Invoker<P>) invoker).invoke(listener, player, entity);
        return res;
    }

    @FunctionalInterface
    private interface Invoker<P extends Player> {
        boolean invoke(DiscoveryEventListener<P> listener, P player, Entity entity);
    }
}

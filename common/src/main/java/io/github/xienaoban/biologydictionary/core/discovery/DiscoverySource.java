package io.github.xienaoban.biologydictionary.core.discovery;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public enum DiscoverySource {
    ENTITY_DETAIL_SCREEN(DiscoveryEventListener::onEntityDetailScreenOpened),
    HIGHLIGHT(DiscoveryEventListener::onEntityHighlighted),
    TELESCOPE_OBSERVE(DiscoveryEventListener::onEntityObservedWithTelescope),
    INTERACT(DiscoveryEventListener::onEntityInteracted),
    KILL(DiscoveryEventListener::onEntityKilled),
    KILLED_BY(DiscoveryEventListener::onPlayerKilledBy),
    UNKNOWN((listener, player, entity) -> false);

    private final Invoker<?> invoker;

    DiscoverySource(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    public <P extends Player> boolean dispatch(DiscoveryEventListener<P> listener, P player, Entity entity) {
        @SuppressWarnings("unchecked")
        boolean result = ((Invoker<P>) invoker).invoke(listener, player, entity);
        return result;
    }

    @FunctionalInterface
    private interface Invoker<P extends Player> {
        boolean invoke(DiscoveryEventListener<P> listener, P player, Entity entity);
    }
}

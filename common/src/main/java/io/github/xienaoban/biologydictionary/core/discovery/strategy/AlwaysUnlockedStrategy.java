package io.github.xienaoban.biologydictionary.core.discovery.strategy;

import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryStrategy;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * All entities are always discovered. No events trigger any action.
 */
public final class AlwaysUnlockedStrategy implements DiscoveryStrategy {
    public static final AlwaysUnlockedStrategy INSTANCE = new AlwaysUnlockedStrategy();

    private AlwaysUnlockedStrategy() {}

    @Override
    public boolean isDiscovered(Identifier entityType, ServerPlayer player) {
        return true;
    }
}

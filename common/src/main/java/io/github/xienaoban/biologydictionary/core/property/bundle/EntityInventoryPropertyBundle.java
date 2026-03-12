package io.github.xienaoban.biologydictionary.core.property.bundle;

import io.github.xienaoban.biologydictionary.mixin.entity.AbstractHorseIMixin;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Function;

public final class EntityInventoryPropertyBundle {

    public static final Function<Entity, InventoryHandler<?>> PLAYER_PATTERN = entity -> {
        if (entity instanceof Player) {
            return (InventoryHandler<Entity>) e -> ((Player) e).getInventory();
        }
        return null;
    };

    public static final Function<Entity, InventoryHandler<?>> CARRIER_PATTERN = entity -> {
        if (entity instanceof InventoryCarrier) {
            return (InventoryHandler<Entity>) e -> ((InventoryCarrier) e).getInventory();
        }
        return null;
    };

    public static final Function<Entity, InventoryHandler<?>> ABSTRACT_HORSE_PATTERN = entity -> {
        if (entity instanceof AbstractHorse) {
            return (InventoryHandler<Entity>) e -> ((AbstractHorseIMixin) e).biologydictionary$getInventory();
        }
        return null;
    };

    private static final Bundle<InventoryHandler<?>> BUNDLE = new Bundle<>();

    public static void init() {
        register(PLAYER_PATTERN);
        register(CARRIER_PATTERN);
        register(ABSTRACT_HORSE_PATTERN);
    }

    public static void register(Function<Entity, InventoryHandler<?>> pattern) {
        BUNDLE.register(pattern);
    }

    public static <E extends Entity> InventoryHandler<E> getHandler(E entity) {
        List<InventoryHandler<E>> list = Misc.cast(BUNDLE.getHandlers(entity));
        if (list.isEmpty()) { return null; }
        return list.getFirst();
    }

    public static <E extends Entity> Container getContainer(E entity) {
        InventoryHandler<E> handler = getHandler(entity);
        if (handler == null) { return null; }
        return handler.getContainer(entity);
    }

    public static <E extends Entity> Container getContainerOrEmpty(E entity) {
        Container container = getContainer(entity);
        if (container == null) {
            return new SimpleContainer(0);
        }
        return container;
    }

    public interface InventoryHandler<E extends Entity> {
        Container getContainer(E entity);
    }
}

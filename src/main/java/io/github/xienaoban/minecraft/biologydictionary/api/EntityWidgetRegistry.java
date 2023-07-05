package io.github.xienaoban.minecraft.biologydictionary.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.List;

public interface EntityWidgetRegistry<E extends Entity> {
    Class<E> getEntityClass();

    @Environment(EnvType.CLIENT)
    EntityWidgetFactory<E> getWidgetFactory();

    default List<BufInteractor> getBufInteractors() {
        return Collections.emptyList();
    }

     interface BufInteractor {
        void read(FriendlyByteBuf buf);
        void write(FriendlyByteBuf buf, Object... args);
    }
}

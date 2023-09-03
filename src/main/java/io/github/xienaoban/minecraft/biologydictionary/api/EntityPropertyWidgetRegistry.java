package io.github.xienaoban.minecraft.biologydictionary.api;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.List;

public interface EntityPropertyWidgetRegistry<E extends Entity> {
    Class<E> getEntityClass();

    @Environment(EnvType.CLIENT)
    EntityPropertyWidgetFactory<E> getWidgetFactory();

    default List<BufInteractor> getBufInteractors() {
        return Collections.emptyList();
    }

     interface BufInteractor {
        void read(FriendlyByteBuf buf);
        void write(FriendlyByteBuf buf, Object... args);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    interface EntityPropertyWidgetFactory<E extends Entity> {
        EntityPropertyWidget<E> create(E entity);
    }
}

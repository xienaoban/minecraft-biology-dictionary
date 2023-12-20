package io.github.xienaoban.minecraft.biologydictionary.api;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

import java.util.Collections;
import java.util.Map;

public interface EntityPropertyWidgetRegistry<E extends Entity> {
    Class<E> getEntityClass();

    @Environment(EnvType.CLIENT)
    EntityPropertyWidgetFactory<E> getWidgetFactory();

    default Map<String, EntitySettingBufHandler<?>> getEntitySettingBufHandles() {
        return Collections.emptyMap();
    }

    default Map<String, EntityDataBufHandler<?>> getEntityDataBufHandlers() {
        return Collections.emptyMap();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    interface EntityPropertyWidgetFactory<E extends Entity> {
        EntityPropertyWidget<E> create(E entity);
    }

    /**
     * Send entity setting from the client.
     */
    interface EntitySettingBufHandler<E extends Entity> {
        /**
         * Read the buf sent from the client.
         * Invoked on the server.
         */
        void read(FriendlyByteBuf buf, E entity);

        /**
         * Write the setting to the buf.
         * Invoked on the client.
         */
        void write(FriendlyByteBuf buf, E entity, Object... args);
    }

    /**
     * Send entity data from the server.
     */
    interface EntityDataBufHandler<E extends Entity> {
        /**
         * Read the tag sent from the server.
         * Invoked on the client.
         */
        void read(CompoundTag tag, E entity);

        /**
         * Write the data to the buf.
         * Invoked on the server.
         */
        void write(CompoundTag tag, E entity);
    }
}

package io.github.xienaoban.minecraft.biologydictionary.api;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

public interface EntityPropertyWidgetRegistry<E extends Entity> {
    Class<E> getEntityClass();

    @Environment(EnvType.CLIENT)
    EntityPropertyWidget<E> createWidget(E entity);
}

package io.github.xienaoban.minecraft.biologydictionary.api;


import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface EntityWidgetFactory<E extends Entity> {
    EntityWidget<E> create(E entity);
}
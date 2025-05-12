package io.github.xienaoban.minecraft.biologydictionary.api;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface EntityPropertyWidgetRegister {
    <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz);
}

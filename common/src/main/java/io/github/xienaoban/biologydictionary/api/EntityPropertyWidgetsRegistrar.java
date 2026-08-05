package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.Entity;

/**
 * Registration handle for the client-side widget registry, passed to {@link EntityPropertyWidgetsPlugin}.
 */
@ClientOnly
public interface EntityPropertyWidgetsRegistrar {
    <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                                     EntityPropertyWidget.Factory<E> widgetFactory);
}

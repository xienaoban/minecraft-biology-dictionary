package io.github.xienaoban.biologydictionary.api.plugin;

import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.Entity;

/**
 * Client-only plugin for registering entity property widgets. Discovered and dispatched exactly once
 * during client initialization, before the widget registry freezes.
 */
@ClientOnly
public interface EntityPropertyWidgetsPlugin {
    void registerEntityPropertyWidgets(EntityPropertyWidgetsPlugin.Registrar registrar);

    @ClientOnly
    interface Registrar {
        <E extends Entity> void register(Class<? extends EntityPropertyWidget<E>> widgetClazz,
                                         EntityPropertyWidget.Factory<E> widgetFactory);
    }
}

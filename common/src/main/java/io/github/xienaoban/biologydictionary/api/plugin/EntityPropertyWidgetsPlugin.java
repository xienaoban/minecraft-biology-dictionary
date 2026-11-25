package io.github.xienaoban.biologydictionary.api.plugin;

import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
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

        /**
         * Hides the widget for the given entity class and all of its subclasses, no matter whether any other
         * widget covers the same information. Use it when the widget must never show up there, e.g. when a
         * subclass has been derived from the entity the widget is aimed at.
         * <p>
         * Rules are validated once registration finishes: an unregistered widget class, or an entity class that
         * has no inheritance relation with the class the widget is registered for, is a hard error.
         */
        void exclude(Class<? extends EntityPropertyWidget<?>> widgetClazz,
                     Class<? extends Entity> entityClazz);

        /**
         * Hides {@code oldWidgetClazz} on entities where {@code newWidgetClazz} has actually been created, and
         * leaves it visible where it has not, so a specialized widget can take over without leaving a gap when
         * its own creation fails.
         * <p>
         * Both widgets must already be registered, in any order relative to this call. For a many-to-one
         * replacement, call this once per superseded widget. Replacement never changes display order: the new
         * widget stays at its own registration slot rather than inheriting the old one's.
         */
        void replace(Class<? extends EntityPropertyWidget<?>> oldWidgetClazz,
                     Class<? extends EntityPropertyWidget<?>> newWidgetClazz);
    }
}

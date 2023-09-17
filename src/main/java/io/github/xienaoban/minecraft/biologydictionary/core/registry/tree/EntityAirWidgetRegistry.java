package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree.EntityAirWidget;
import net.minecraft.world.entity.Entity;

public class EntityAirWidgetRegistry implements EntityPropertyWidgetRegistry<Entity> {
    @Override
    public Class<Entity> getEntityClass() { return Entity.class; }

    @Override
    public EntityPropertyWidgetFactory<Entity> getWidgetFactory() { return EntityAirWidget::new; }
}

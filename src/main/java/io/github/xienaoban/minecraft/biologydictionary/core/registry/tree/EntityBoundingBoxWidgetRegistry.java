package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree.EntityBoundingBoxWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

public class EntityBoundingBoxWidgetRegistry implements EntityPropertyWidgetRegistry<Entity> {
    @Override
    public Class<Entity> getEntityClass() { return Entity.class; }

    @Environment(EnvType.CLIENT)
    @Override
    public EntityPropertyWidgetFactory<Entity> getWidgetFactory() { return EntityBoundingBoxWidget::new; }
}

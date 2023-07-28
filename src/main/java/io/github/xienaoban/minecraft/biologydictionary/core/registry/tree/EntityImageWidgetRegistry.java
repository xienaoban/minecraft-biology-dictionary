package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitywidget.tree.EntityImageWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

public class EntityImageWidgetRegistry implements EntityWidgetRegistry<Entity> {
    @Override
    public Class<Entity> getEntityClass() { return Entity.class; }

    @Environment(EnvType.CLIENT)
    @Override
    public EntityWidgetFactory<Entity> getWidgetFactory() { return EntityImageWidget::new; }
}


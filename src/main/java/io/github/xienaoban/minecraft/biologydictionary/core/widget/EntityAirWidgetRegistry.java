package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

public class EntityAirWidgetRegistry implements EntityPropertyWidgetRegistry<Entity> {
    @Override
    public Class<Entity> getEntityClass() { return Entity.class; }

    @Environment(EnvType.CLIENT)
    @Override
    public EntityPropertyWidgetFactory<Entity> getWidgetFactory() { return EntityAirWidget::new; }
}

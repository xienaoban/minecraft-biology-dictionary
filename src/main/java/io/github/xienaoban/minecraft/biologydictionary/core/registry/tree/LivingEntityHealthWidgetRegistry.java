package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree.LivingEntityHealthWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityHealthWidgetRegistry implements EntityPropertyWidgetRegistry<LivingEntity> {
    @Override
    public Class<LivingEntity> getEntityClass() { return LivingEntity.class; }

    @Environment(EnvType.CLIENT)
    @Override
    public EntityPropertyWidgetFactory<LivingEntity> getWidgetFactory() { return LivingEntityHealthWidget::new; }
}

package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitywidget.tree.LivingEntityHealthWidget;
import net.minecraft.world.entity.LivingEntity;

public class LivingEntityHealthWidgetRegistry implements EntityWidgetRegistry<LivingEntity> {
    @Override
    public Class<LivingEntity> getEntityClass() { return LivingEntity.class; }

    @Override
    public EntityWidgetFactory<LivingEntity> getWidgetFactory() { return LivingEntityHealthWidget::new; }
}

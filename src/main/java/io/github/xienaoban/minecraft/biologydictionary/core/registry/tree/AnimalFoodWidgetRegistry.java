package io.github.xienaoban.minecraft.biologydictionary.core.registry.tree;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
import io.github.xienaoban.minecraft.biologydictionary.gui.entitypropertywidget.tree.AnimalFoodWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Animal;

public class AnimalFoodWidgetRegistry implements EntityPropertyWidgetRegistry<Animal> {
    @Override
    public Class<Animal> getEntityClass() { return Animal.class; }

    @Environment(EnvType.CLIENT)
    @Override
    public EntityPropertyWidgetFactory<Animal> getWidgetFactory() {
        return AnimalFoodWidget::new;
    }
}

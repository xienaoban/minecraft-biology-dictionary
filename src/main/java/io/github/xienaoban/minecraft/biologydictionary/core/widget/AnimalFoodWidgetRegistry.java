package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.api.EntityPropertyWidgetRegistry;
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

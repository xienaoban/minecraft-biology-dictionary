package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import net.minecraft.world.entity.animal.Animal;

public class MobTemptWidget extends EntityPropertyStandardWidget<Animal> {
    protected MobTemptWidget(EntityProperties<Animal> properties) {
        super(properties);
    }
}

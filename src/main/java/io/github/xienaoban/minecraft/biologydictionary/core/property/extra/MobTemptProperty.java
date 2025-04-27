package io.github.xienaoban.minecraft.biologydictionary.core.property.extra;

import io.github.xienaoban.minecraft.biologydictionary.common.property.ItemStackListProperty;
import net.minecraft.world.entity.Mob;

public class MobTemptProperty extends ItemStackListProperty<Mob> {
    public MobTemptProperty() {
        super(MobTemptProperty.class.getSimpleName());
    }

    @Override
    public void writeTo(Mob entity) {
        super.writeTo(entity);
    }

    @Override
    public void readFrom(Mob entity) {
        super.readFrom(entity);
    }
}

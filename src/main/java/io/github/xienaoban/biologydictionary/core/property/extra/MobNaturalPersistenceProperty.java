package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import net.minecraft.world.entity.Mob;

public class MobNaturalPersistenceProperty extends BooleanProperty<Mob> {
    public static final Factory<Mob> FACTORY = MobNaturalPersistenceProperty::new;

    public MobNaturalPersistenceProperty() {
        super(MobNaturalPersistenceProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Mob entity) {
        setVal(entity.requiresCustomPersistence() || !entity.removeWhenFarAway(Double.MAX_VALUE));
    }

    @Override
    public void setTo(Mob entity) {
        throw new UnsupportedOperationException();
    }
}

package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;

public class EntityInventorySizeProperty extends IntProperty<Entity> {
    public static final Factory<Entity> FACTORY = EntityInventorySizeProperty::new;

    public EntityInventorySizeProperty() {
        super(EntityInventorySizeProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Entity entity) {
        Container container = EntityInventoryPropertyBundle.getContainer(entity);
        if (container != null) {
            setVal(container.getContainerSize());
        } else {
            setVal(null);
        }
    }

    @Override
    public void setTo(Entity entity) {
        throw new UnsupportedOperationException();
    }
}

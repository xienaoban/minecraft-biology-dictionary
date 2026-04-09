package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class ItemStackProperty<E extends Entity> extends AbstractProperty<E, ItemStack> {
    public ItemStackProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(ItemStack.parseOptional(WorldSession.justGiveMeALevel().registryAccess(), nbt.getCompound(name())));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null && !getVal().isEmpty()) {
            nbt.put(name(), getVal().save(WorldSession.justGiveMeALevel().registryAccess()));
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

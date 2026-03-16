package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class ItemStackProperty<E extends Entity> extends AbstractProperty<E, ItemStack> {

    public ItemStackProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        CompoundTag tag = nbt.getCompound(name());
        setVal(tag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(tag));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null && !getVal().isEmpty()) {
            CompoundTag tag = new CompoundTag();
            getVal().save(tag);
            nbt.put(name(), tag);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

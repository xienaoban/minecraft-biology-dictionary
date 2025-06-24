package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemStackListProperty<E extends Entity> extends AbstractProperty<E, List<ItemStack>> {
    public ItemStackListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), ItemStack.CODEC.listOf()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), ItemStack.CODEC.listOf(), get());
    }
}

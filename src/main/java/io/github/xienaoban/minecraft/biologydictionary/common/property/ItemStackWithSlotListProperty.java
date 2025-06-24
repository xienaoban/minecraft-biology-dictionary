package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class ItemStackWithSlotListProperty<E extends Entity> extends AbstractProperty<E, List<ItemStackWithSlot>> {
    public ItemStackWithSlotListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(nbt.read(name(), ItemStackWithSlot.CODEC.listOf()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), ItemStackWithSlot.CODEC.listOf(), get());
    }
}

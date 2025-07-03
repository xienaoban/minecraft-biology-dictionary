package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ItemStackWithSlotListProperty<E extends Entity> extends CodecProperty<E, List<ItemStackWithSlot>> {
    public ItemStackWithSlotListProperty(String propertyName) {
        super(propertyName, ItemStackWithSlot.CODEC.listOf());
    }
}

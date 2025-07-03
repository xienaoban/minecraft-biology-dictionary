package io.github.xienaoban.minecraft.biologydictionary.core.property.builtin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemStackListProperty<E extends Entity> extends CodecProperty<E, List<ItemStack>> {
    public ItemStackListProperty(String propertyName) {
        super(propertyName, ItemStack.CODEC.listOf());
    }
}

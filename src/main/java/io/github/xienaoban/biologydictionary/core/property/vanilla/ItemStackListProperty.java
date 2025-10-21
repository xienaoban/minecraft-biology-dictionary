package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemStackListProperty<E extends Entity> extends CodecProperty<E, List<ItemStack>> {
    public ItemStackListProperty(String propertyName) {
        super(propertyName, List.class, ItemStack.CODEC.listOf());
    }
}

package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.BD;

public class ItemStackProperty<E extends Entity> extends AbstractProperty<E, ItemStack> {
    public ItemStackProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        setVal(ItemStack.parseOptional(BD.justGiveMeALevel().registryAccess(), nbt.getCompound(name())));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null && !getVal().isEmpty()) {
            nbt.put(name(), getVal().save(BD.justGiveMeALevel().registryAccess()));
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

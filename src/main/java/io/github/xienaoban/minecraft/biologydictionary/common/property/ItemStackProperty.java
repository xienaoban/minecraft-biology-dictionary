package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemStackProperty<E extends Entity> extends AbstractProperty<E, ItemStack> {
    private final RegistryAccess registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();

    public ItemStackProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        set(ItemStack.parseOptional(registryAccess, nbt.getCompound(name())));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null && !get().isEmpty()) {
            nbt.put(name(), get().save(registryAccess));
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

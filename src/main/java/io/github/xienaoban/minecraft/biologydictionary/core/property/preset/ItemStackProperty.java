package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ItemStackProperty extends AbstractProperty<ItemStack> {
    private final RegistryAccess registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();

    public ItemStackProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        set(ItemStack.parseOptional(registryAccess, vanillaNbt.getCompound(name())));
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null && !get().isEmpty()) {
            vanillaNbt.put(name(), get().save(registryAccess));
        } else {
            vanillaNbt.put(name(), new CompoundTag());
        }
    }
}

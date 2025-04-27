package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class ItemStackListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<ItemStack>> {
    private final RegistryAccess registryAccess = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();

    public ItemStackListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_COMPOUND);
            ArrayList<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(ItemStack.parseOptional(registryAccess, listTag.getCompound(i)));
            }
            set(list);
        } else {
            set(new ArrayList<>());
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            ListTag listTag = new ListTag();
            for (var e : get()) {
                if (e != null && !e.isEmpty()) {
                    listTag.add(e.save(registryAccess));
                } else {
                    listTag.add(new CompoundTag());
                }
            }
            nbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

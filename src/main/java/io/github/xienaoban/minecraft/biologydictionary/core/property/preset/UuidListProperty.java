package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class UuidListProperty extends AbstractProperty<ArrayList<UUID>> {
    public UuidListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = vanillaNbt.getList(name(), Tag.TAG_INT_ARRAY);
            ArrayList<UUID> list = new ArrayList<>();
            for (Tag tag : listTag) {
                list.add(NbtUtils.loadUUID(tag));
            }
            set(list);
        } else {
            set(new ArrayList<>());
        }
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            ListTag listTag = new ListTag();
            for (var e : get()) {
                listTag.add(NbtUtils.createUUID(e));
            }
            vanillaNbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

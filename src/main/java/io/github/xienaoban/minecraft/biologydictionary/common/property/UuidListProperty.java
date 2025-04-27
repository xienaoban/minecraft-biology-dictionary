package io.github.xienaoban.minecraft.biologydictionary.common.property;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.UUID;

public final class UuidListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<UUID>> {
    public UuidListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_INT_ARRAY);
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
    public void writeTo(CompoundTag nbt) {
        if (get() != null) {
            ListTag listTag = new ListTag();
            for (var e : get()) {
                listTag.add(NbtUtils.createUUID(e));
            }
            nbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

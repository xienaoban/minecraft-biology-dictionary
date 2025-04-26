package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public final class IntListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<Integer>> {
    public IntListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_INT);
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getInt(i));
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
                listTag.add(IntTag.valueOf(e));
            }
            nbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public final class FloatListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<Float>> {
    public FloatListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_FLOAT);
            ArrayList<Float> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getFloat(i));
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
                listTag.add(FloatTag.valueOf(e));
            }
            nbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;

public final class DoubleListProperty extends AbstractProperty<ArrayList<Double>> {
    public DoubleListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = vanillaNbt.getList(name(), Tag.TAG_DOUBLE);
            ArrayList<Double> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getDouble(i));
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
                listTag.add(DoubleTag.valueOf(e));
            }
            vanillaNbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

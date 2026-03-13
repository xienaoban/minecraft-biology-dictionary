package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class DoubleListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<Double>> {
    public DoubleListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_DOUBLE);
            ArrayList<Double> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getDouble(i));
            }
            setVal(list);
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            ListTag listTag = new ListTag();
            for (var e : getVal()) {
                listTag.add(DoubleTag.valueOf(e));
            }
            nbt.put(name(), listTag);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

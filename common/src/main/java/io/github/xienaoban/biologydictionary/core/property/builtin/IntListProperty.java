package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class IntListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<Integer>> {
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
                listTag.add(IntTag.valueOf(e));
            }
            nbt.put(name(), listTag);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

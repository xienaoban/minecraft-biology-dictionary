package io.github.xienaoban.biologydictionary.core.property.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class StringListProperty<E extends Entity> extends AbstractProperty<E, ArrayList<String>> {
    public StringListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag nbtList = nbt.getList(name(), Tag.TAG_STRING);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < nbtList.size(); i++) {
                list.add(nbtList.getString(i));
            }
            setVal(list);
        } else {
            setVal(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            ListTag nbtList = new ListTag();
            for (var e : getVal()) {
                nbtList.add(StringTag.valueOf(e));
            }
            nbt.put(name(), nbtList);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

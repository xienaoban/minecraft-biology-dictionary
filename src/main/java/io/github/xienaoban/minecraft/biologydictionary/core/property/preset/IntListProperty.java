package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.*;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public final class IntListProperty extends AbstractProperty<ArrayList<Integer>> {
    public IntListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = vanillaNbt.getList(name(), Tag.TAG_INT);
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
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            ListTag listTag = new ListTag();
            for (var e : get()) {
                listTag.add(IntTag.valueOf(e));
            }
            vanillaNbt.put(name(), listTag);
        } else {
            throw new IllegalPropertyStateException("list type must not be null");
        }
    }
}

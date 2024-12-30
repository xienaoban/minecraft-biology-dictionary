package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public final class StringListProperty extends AbstractProperty<ArrayList<String>> {
    public StringListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = vanillaNbt.getList(name(), Tag.TAG_STRING);
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getString(i));
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
                listTag.add(StringTag.valueOf(e));
            }
            vanillaNbt.put(name(), listTag);
        } else {
            vanillaNbt.remove(name());
        }
    }
}

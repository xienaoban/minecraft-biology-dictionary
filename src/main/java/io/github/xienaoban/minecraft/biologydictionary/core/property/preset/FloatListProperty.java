package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.*;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public final class FloatListProperty extends AbstractProperty<ArrayList<Float>> {
    public FloatListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        if (vanillaNbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = vanillaNbt.getList(name(), Tag.TAG_FLOAT);
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
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            ListTag listTag = new ListTag();
            for (var e : get()) {
                listTag.add(FloatTag.valueOf(e));
            }
            vanillaNbt.put(name(), listTag);
        } else {
            vanillaNbt.remove(name());
        }
    }
}

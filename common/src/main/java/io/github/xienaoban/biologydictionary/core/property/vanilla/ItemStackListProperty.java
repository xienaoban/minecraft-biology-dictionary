package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackListProperty<E extends Entity> extends AbstractProperty<E, List<ItemStack>> {

    public ItemStackListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_COMPOUND);
            ArrayList<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                list.add(tag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(tag));
            }
            setVal(list);
        } else {
            setVal(new ArrayList<>());
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() != null) {
            ListTag listTag = new ListTag();
            for (var e : getVal()) {
                if (e != null && !e.isEmpty()) {
                    CompoundTag tag = new CompoundTag();
                    listTag.add(e.save(tag));
                } else {
                    listTag.add(new CompoundTag());
                }
            }
            nbt.put(name(), listTag);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Property for storing a list of {@link MobEffectInstance}.
 * Used for LivingEntity's active_effects NBT tag.
 *
 * @see LivingEntity#getActiveEffects()
 */
public class MobEffectInstanceListProperty<E extends Entity> extends AbstractProperty<E, List<MobEffectInstance>> {
    public MobEffectInstanceListProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_COMPOUND);
            ArrayList<MobEffectInstance> list = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag compoundTag = listTag.getCompound(i);
                MobEffectInstance effect = MobEffectInstance.load(compoundTag);
                if (effect != null) {
                    list.add(effect);
                }
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
            for (MobEffectInstance effect : getVal()) {
                listTag.add(effect.save());
            }
            nbt.put(name(), listTag);
        } else {
            nbt.put(name(), new CompoundTag());
        }
    }
}

package io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla;

import io.github.xienaoban.minecraft.biologydictionary.common.property.AbstractProperty;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class LivingEntityActiveEffectsProperty extends AbstractProperty<LivingEntity, Map<Holder<MobEffect>, MobEffectInstance>> {

    public LivingEntityActiveEffectsProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        Map<Holder<MobEffect>, MobEffectInstance> activeEffects = new HashMap<>();
        if (nbt.contains(name(), Tag.TAG_LIST)) {
            ListTag listTag = nbt.getList(name(), Tag.TAG_COMPOUND);

            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                MobEffectInstance mobEffectInstance = MobEffectInstance.load(tag);
                if (mobEffectInstance != null) {
                    activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
                }
            }
        }
        set(activeEffects);
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        throw new IllegalPropertyStateException("not implemented");
    }
}

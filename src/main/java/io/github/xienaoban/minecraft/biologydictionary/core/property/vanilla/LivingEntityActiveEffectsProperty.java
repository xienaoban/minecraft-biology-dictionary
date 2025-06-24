package io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla;

import io.github.xienaoban.minecraft.biologydictionary.common.property.AbstractProperty;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LivingEntityActiveEffectsProperty extends AbstractProperty<LivingEntity, Map<Holder<MobEffect>, MobEffectInstance>> {

    public LivingEntityActiveEffectsProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        List<MobEffectInstance> list = nbt.read(name(), MobEffectInstance.CODEC.listOf()).orElse(null);
        if (list != null) {
            Map<Holder<MobEffect>, MobEffectInstance> activeEffects = new HashMap<>();
            for (MobEffectInstance mobEffectInstance : list) {
                activeEffects.put(mobEffectInstance.getEffect(), mobEffectInstance);
            }
            set(activeEffects);
        } else {
            set(null);
        }
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        nbt.storeNullable(name(), MobEffectInstance.CODEC.listOf(), List.copyOf(get().values()));
    }
}

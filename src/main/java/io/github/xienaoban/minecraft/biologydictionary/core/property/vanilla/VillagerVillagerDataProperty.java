package io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla;

import io.github.xienaoban.minecraft.biologydictionary.common.property.AbstractProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public final class VillagerVillagerDataProperty extends AbstractProperty<Villager, VillagerData> {
    public VillagerVillagerDataProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        VillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get(name()))
                .resultOrPartial(LOGGER::error)
                .ifPresentOrElse(this::set, () -> set(null));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (get() == null) { return; }
        VillagerData.CODEC.encodeStart(NbtOps.INSTANCE, get())
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> nbt.put(name(), tag));
    }
}

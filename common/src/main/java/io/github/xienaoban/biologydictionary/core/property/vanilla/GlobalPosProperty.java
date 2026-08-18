package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class GlobalPosProperty<E extends Entity> extends AbstractProperty<E, GlobalPos> {
    public GlobalPosProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        CompoundTag x =  nbt.getCompound(name());
        ResourceLocation location = IdentifierUtils.fromStringOrNull(x.getString("rl"));
        if (location == null) {
            setVal(null);
            return;
        }
        ResourceKey<Level> resourceKey = ResourceKey.create(Registries.DIMENSION, location);
        CompoundTag bpTag = x.getCompound("bp");
        if (bpTag.isEmpty()) {
            setVal(null);
            return;
        }
        BlockPos blockPos = NbtUtils.readBlockPos(bpTag);
        GlobalPos globalPos = GlobalPos.of(resourceKey, blockPos);
        setVal(globalPos);
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        CompoundTag x = new CompoundTag();
        if (getVal() != null) {
            GlobalPos globalPos = getVal();
            x.putString("rl", IdentifierUtils.toString(globalPos.dimension().location()));
            x.put("bp", NbtUtils.writeBlockPos(globalPos.pos()));
        }
        nbt.put(name(), x);
    }
}

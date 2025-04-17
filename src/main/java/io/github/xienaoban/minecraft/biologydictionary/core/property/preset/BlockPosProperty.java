package io.github.xienaoban.minecraft.biologydictionary.core.property.preset;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public final class BlockPosProperty extends AbstractProperty<BlockPos> {
    public BlockPosProperty(String propertyName) {
        super(propertyName);
    }

    @Override
    public void readFrom(CompoundTag vanillaNbt) {
        set(NbtUtils.readBlockPos(vanillaNbt, name()).orElse(null));
    }

    @Override
    public void writeTo(CompoundTag vanillaNbt) {
        if (get() != null) {
            vanillaNbt.put(name(), NbtUtils.writeBlockPos(get()));
        } else {
            vanillaNbt.put(name(), new CompoundTag());
        }
    }
}

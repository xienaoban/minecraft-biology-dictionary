package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public class BlockPosProperty<E extends Entity> extends CodecProperty<E, BlockPos> {
    public BlockPosProperty(String propertyName) {
        super(propertyName, BlockPos.CODEC);
    }
}

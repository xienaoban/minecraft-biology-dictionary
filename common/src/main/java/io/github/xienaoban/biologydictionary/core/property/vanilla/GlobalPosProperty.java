package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;

public class GlobalPosProperty<E extends Entity> extends CodecProperty<E, GlobalPos> {
    public GlobalPosProperty(String propertyName) {
        super(propertyName, GlobalPos.class, GlobalPos.CODEC);
    }
}

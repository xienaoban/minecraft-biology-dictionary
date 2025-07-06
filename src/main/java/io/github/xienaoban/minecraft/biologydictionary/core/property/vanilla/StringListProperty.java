package io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla;

import com.mojang.serialization.Codec;
import io.github.xienaoban.minecraft.biologydictionary.core.property.builtin.CodecProperty;
import net.minecraft.world.entity.Entity;

import java.util.List;

public final class StringListProperty<E extends Entity> extends CodecProperty<E, List<String>> {
    public StringListProperty(String propertyName) {
        super(propertyName, Codec.STRING.listOf());
    }
}

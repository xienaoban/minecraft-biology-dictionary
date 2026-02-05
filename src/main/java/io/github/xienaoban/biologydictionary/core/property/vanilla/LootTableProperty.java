package io.github.xienaoban.biologydictionary.core.property.vanilla;

import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableProperty<E extends Entity> extends CodecProperty<E, LootTable> {
    public LootTableProperty(String propertyName) {
        super(propertyName, LootTable.class, LootTable.DIRECT_CODEC);
    }
}

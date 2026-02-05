package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.vanilla.LootTableProperty;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;

public class EntityLootTableProperty extends LootTableProperty<Entity> {
    public static final Factory<Entity> FACTORY = EntityLootTableProperty::new;

    public EntityLootTableProperty() {
        super(EntityLootTableProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Entity entity) {
        Optional<ResourceKey<LootTable>> key = entity.getLootTable();
        if (key.isEmpty()) {
            setVal(null);
        } else {
            LootTable lootTable = Objects.requireNonNull(EntityUtils.getLevel(entity).getServer())
                    .reloadableRegistries()
                    .getLootTable(key.get());
            setVal(lootTable);
        }
    }

    @Override
    public void setTo(Entity entity) {
        throw new UnsupportedOperationException();
    }
}

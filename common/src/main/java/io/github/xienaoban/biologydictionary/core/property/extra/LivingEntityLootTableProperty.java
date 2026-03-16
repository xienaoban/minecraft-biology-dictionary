package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.LootTableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LivingEntityLootTableProperty extends AbstractProperty<LivingEntity, List<LootTableUtils.LootEntry>> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityLootTableProperty::new;

    public LivingEntityLootTableProperty() {
        super(LivingEntityLootTableProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(LivingEntity entity) {
        ResourceLocation key = LootTableUtils.getLootTableKey(entity);
        if (key == null) {
            setVal(null);
        } else {
            LootTable lootTable = Objects.requireNonNull(EntityUtils.getLevel(entity).getServer())
                    .getLootData()
                    .getLootTable(key);
            setVal(LootTableUtils.parseLootEntries(lootTable, entity));
        }
    }

    @Override
    public void setTo(LivingEntity entity) {
        throw new UnsupportedOperationException("Cannot set loot table on entity");
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (!nbt.contains(name())) {
            setVal(null);
            return;
        }

        ListTag entries = nbt.getList(name(), Tag.TAG_COMPOUND);
        List<LootTableUtils.LootEntry> result = new ArrayList<>(entries.size());
        for (Tag entry : entries) {
            result.add(LootTableUtils.LootEntry.fromNbt((CompoundTag) entry));
        }
        setVal(result);
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        if (getVal() == null) {
            return;
        }

        ListTag entries = new ListTag();
        for (LootTableUtils.LootEntry entry : getVal()) {
            entries.add(entry.toNbt());
        }
        nbt.put(name(), entries);
    }
}

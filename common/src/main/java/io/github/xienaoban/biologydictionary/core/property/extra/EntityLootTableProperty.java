package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.LootTableUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityLootTableProperty extends AbstractProperty<Entity, List<LootTableUtils.LootEntry>> {
    public static final Factory<Entity> FACTORY = EntityLootTableProperty::new;

    public EntityLootTableProperty() {
        super(EntityLootTableProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Entity entity) {
        WorldSession ws = WorldSession.get();
        if (ws == null) {
            setVal(getParsedLootEntries(entity));
            return;
        }
        setVal(ws.getStaticEntityPropertyCache().getOrCompute(
                EntityUtils.getEntityType(entity), EntityLootTableProperty.class,
                () -> getParsedLootEntries(entity)));
    }

    @Override
    public void setTo(Entity entity) {
        throw new UnsupportedOperationException("Cannot set loot table on entity");
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (!nbt.contains(name())) {
            setVal(null);
            return;
        }

        ListTag entries = nbt.getList(name()).orElse(new ListTag());
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

    private static List<LootTableUtils.LootEntry> getParsedLootEntries(Entity entity) {
        Optional<ResourceKey<LootTable>> key = LootTableUtils.getLootTableKey(entity);
        if (key.isEmpty()) {
            return null;
        }
        LootTable lootTable = Objects.requireNonNull(EntityUtils.getLevel(entity).getServer())
                .reloadableRegistries()
                .getLootTable(key.get());
        return LootTableUtils.parseLootEntries(lootTable);
    }
}

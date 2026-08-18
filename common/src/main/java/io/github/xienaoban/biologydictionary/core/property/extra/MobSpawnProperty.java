package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.EntitySpawnManager;
import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MobSpawnProperty extends AbstractProperty<Mob, MobSpawnProperty.Data> {
    public static final Factory<Mob> FACTORY = MobSpawnProperty::new;

    private static final String BIOMES_KEY = "biomes";
    private static final String STRUCTURES_KEY = "structures";

    public MobSpawnProperty() {
        super(MobSpawnProperty.class.getSimpleName());
    }

    @Override
    public void getFrom(Mob entity) {
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws == null) {
            setVal(null);
            return;
        }
        EntitySpawnManager manager = sws.getEntitySpawnManager();
        List<ResourceLocation> biomes = List.copyOf(manager.getSpawnBiomes(EntityUtils.getEntityType(entity)));
        List<ResourceLocation> structures = List.copyOf(manager.getSpawnStructures(EntityUtils.getEntityType(entity)));
        setVal(new Data(biomes, structures));
    }

    @Override
    public void setTo(Mob entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readFrom(CompoundTag nbt) {
        if (!nbt.contains(name())) {
            setVal(null);
            return;
        }
        CompoundTag tag = nbt.getCompound(name());

        List<ResourceLocation> biomes = new ArrayList<>();
        ListTag biomeList = tag.getList(BIOMES_KEY, Tag.TAG_STRING);
        for (Tag t : biomeList) {
            biomes.add(Objects.requireNonNull(IdentifierUtils.fromNbt((StringTag) t)));
        }

        List<ResourceLocation> structures = new ArrayList<>();
        ListTag structureList = tag.getList(STRUCTURES_KEY, Tag.TAG_STRING);
        for (Tag t : structureList) {
            structures.add(Objects.requireNonNull(IdentifierUtils.fromNbt((StringTag) t)));
        }

        setVal(new Data(biomes, structures));
    }

    @Override
    public void writeTo(CompoundTag nbt) {
        Data data = getVal();
        if (data == null) {
            return;
        }
        CompoundTag tag = new CompoundTag();

        ListTag biomeList = new ListTag();
        for (ResourceLocation id : data.biomes()) {
            biomeList.add(IdentifierUtils.toNbt(id));
        }
        tag.put(BIOMES_KEY, biomeList);

        ListTag structureList = new ListTag();
        for (ResourceLocation id : data.structures()) {
            structureList.add(IdentifierUtils.toNbt(id));
        }
        tag.put(STRUCTURES_KEY, structureList);

        nbt.put(name(), tag);
    }

    public record Data(List<ResourceLocation> biomes, List<ResourceLocation> structures) {}
}

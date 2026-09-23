package io.github.xienaoban.biologydictionary.core.property.extra;

import io.github.xienaoban.biologydictionary.core.EntitySpawnManager;
import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

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
        if (!NbtUtils.contains(nbt, name())) {
            setVal(null);
            return;
        }
        CompoundTag tag = NbtUtils.getCompound(nbt, name());

        List<ResourceLocation> biomes = new ArrayList<>();
        ListTag biomeList = NbtUtils.getListOr(tag, BIOMES_KEY, new ListTag());
        for (Tag t : biomeList) {
            if (t instanceof StringTag st) {
                biomes.add(IdentifierUtils.fromNbt(st));
            }
        }

        List<ResourceLocation> structures = new ArrayList<>();
        ListTag structureList = NbtUtils.getListOr(tag, STRUCTURES_KEY, new ListTag());
        for (Tag t : structureList) {
            if (t instanceof StringTag st) {
                structures.add(IdentifierUtils.fromNbt(st));
            }
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
        NbtUtils.put(tag, BIOMES_KEY, biomeList);

        ListTag structureList = new ListTag();
        for (ResourceLocation id : data.structures()) {
            structureList.add(IdentifierUtils.toNbt(id));
        }
        NbtUtils.put(tag, STRUCTURES_KEY, structureList);

        NbtUtils.put(nbt, name(), tag);
    }

    public record Data(List<ResourceLocation> biomes, List<ResourceLocation> structures) {}
}

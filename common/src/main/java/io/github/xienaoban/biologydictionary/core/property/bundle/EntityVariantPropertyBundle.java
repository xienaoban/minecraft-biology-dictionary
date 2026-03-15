package io.github.xienaoban.biologydictionary.core.property.bundle;

import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class EntityVariantPropertyBundle {
    private static final Bundle<VariantHandler<?, ?>> BUNDLE = new Bundle<>();
    private static final String NBT_KEY = "V_OR_T"; // Variant or Type

    public static void init() {
        register(STANDARD_PATTERN);

        register(EntityType.VILLAGER, new VillagerTypeHandler());
        register(EntityType.HORSE, new HorseVariantHandler(), new HorseMarkingsHandler());
        register(EntityType.PANDA, new PandaMainGeneHandler(), new PandaHiddenGeneHandler());
    }

    public static void register(Function<Entity, VariantHandler<?, ?>> pattern) {
        BUNDLE.register(pattern);
    }

    public static void register(EntityType<?> entityType, VariantHandler<?, ?>... variantHandlers) {
        BUNDLE.register(entityType, variantHandlers);
    }

    public static <E extends Entity, V> List<VariantHandler<E, V>> getHandlers(E entity) {
        return Misc.cast(BUNDLE.getHandlers(entity));
    }

    public interface VariantHandler<E extends Entity, V> {
        /**
         * Only the first variant handler in the list checks it.
         */
        default boolean isStandard() { return true; }

        List<V> getVariants();
        V getVariant(E entity);
        void setVariant(E entity, V variant);
        CompoundTag variantToNbt(V variant);
        V nbtToVariant(Tag nbt);
        String getVariantName(V variant);
    }

    public interface PropertyVariantHandler<E extends Entity, V> extends VariantHandler<E, V> {
        AbstractProperty<? super E, V> createProperty();

        @Override
        default V getVariant(E entity) {
            return createProperty().withEntity(entity).getVal();
        }

        @Override
        default void setVariant(E entity, V variant) {
            createProperty().withVal(variant).setTo(entity);
        }

        @Override
        default CompoundTag variantToNbt(V variant) {
            return createProperty().withVal(variant).toTag();
        }

        @Override
        default V nbtToVariant(Tag nbt) {
            return createProperty().withTag((CompoundTag) nbt).getVal();
        }
    }

    static final Function<Entity, VariantHandler<?, ?>> STANDARD_PATTERN = entity -> {
        if (!(entity instanceof VariantHolder<?> variantHolder)) {
            return null;
        }
        Object variant = variantHolder.getVariant();
        if (variant instanceof Holder<?> holder) {
            Holder<Object> objHolder = Misc.cast(holder);
            ResourceKey<Object> rk = objHolder.unwrapKey().orElseThrow();
            Registry<Object> registry = entity.level().registryAccess().registry(ResourceKey.createRegistryKey(rk.registry())).orElseThrow();
            List<Holder<Object>> res = registry.registryKeySet().stream()
                    .map(registry::getHolderOrThrow)
                    .map(r -> (Holder<Object>) r)
                    .toList();
            return new HolderHandler(EntityUtils.getEntityType(entity), registry, res);
        } else if (variant instanceof Enum<?>) {
            return new EnumHandler(EntityUtils.getEntityType(entity), variant.getClass());
        }
        return null;
    };

    public record HolderHandler(EntityType<?> entityType, Registry<Object> registry, List<Holder<Object>> variants)
            implements VariantHandler<Entity, Holder<Object>> {

        @Override
        public List<Holder<Object>> getVariants() {
            return variants;
        }

        @Override
        public Holder<Object> getVariant(Entity entity) {
            return Misc.cast(((VariantHolder<?>) entity).getVariant());
        }

        @Override
        public void setVariant(Entity entity, Holder<Object> variant) {
            ((VariantHolder<?>) entity).setVariant(Misc.cast(variant));
        }

        @Override
        public CompoundTag variantToNbt(Holder<Object> variant) {
            ResourceKey<Object> rk = variant.unwrapKey().orElseThrow();
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, rk.location().toString());
            return res;
        }

        @Override
        public Holder<Object> nbtToVariant(Tag nbt) {
            ResourceLocation location = new ResourceLocation(((CompoundTag) nbt).getString(NBT_KEY));
            ResourceKey<Object> key = ResourceKey.create(registry.key(), location);
            return registry.getHolder(key).orElseThrow();
        }

        @Override
        public String getVariantName(Holder<Object> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                ResourceLocation id = resourceKey.location();
                return id.getPath().toLowerCase();
            }).orElse("unknown");
        }
    }

    public record EnumHandler(EntityType<?> entityType, Class<?> clazz) implements VariantHandler<Entity, Enum<?>> {

        @Override
        public List<Enum<?>> getVariants() {
            return Misc.cast(Arrays.stream(clazz.getEnumConstants()).toList());
        }

        @Override
        public Enum<?> getVariant(Entity entity) {
            return Misc.cast(((VariantHolder<?>) entity).getVariant());
        }

        @Override
        public void setVariant(Entity entity, Enum<?> variant) {
            ((VariantHolder<?>) entity).setVariant(Misc.cast(variant));

        }

        @Override
        public CompoundTag variantToNbt(Enum<?> variant) {
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, variant.name());
            return res;
        }

        @Override
        public Enum<?> nbtToVariant(Tag nbt) {
            return Enum.valueOf(Misc.cast(clazz), ((CompoundTag) nbt).getString(NBT_KEY));
        }

        @Override
        public String getVariantName(Enum<?> variant) {
            return variant.name().toLowerCase();
        }
    }

    public static final class VillagerTypeHandler implements VariantHandler<Villager, ResourceKey<VillagerType>> {

        @Override
        public boolean isStandard() { return false; }

        @Override
        public List<ResourceKey<VillagerType>> getVariants() {
            return BuiltInRegistries.VILLAGER_TYPE.registryKeySet().stream().toList();
        }

        @Override
        public ResourceKey<VillagerType> getVariant(Villager entity) {
            VillagerType vt = entity.getVillagerData().getType();
            return BuiltInRegistries.VILLAGER_TYPE.getResourceKey(vt).orElseThrow();
        }

        @Override
        public void setVariant(Villager entity, ResourceKey<VillagerType> variant) {
            entity.setVillagerData(entity.getVillagerData()
                    .setType(Objects.requireNonNull(BuiltInRegistries.VILLAGER_TYPE.get(variant))));
        }

        @Override
        public CompoundTag variantToNbt(ResourceKey<VillagerType> variant) {
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, variant.location().toString());
            return res;
        }

        @Override
        public ResourceKey<VillagerType> nbtToVariant(Tag nbt) {
            ResourceLocation location = new ResourceLocation(((CompoundTag) nbt).getString(NBT_KEY));
            return ResourceKey.create(Registries.VILLAGER_TYPE, location);
        }

        @Override
        public String getVariantName(ResourceKey<VillagerType> variant) {
            ResourceLocation id = variant.location();
            return id.getPath().toLowerCase();
        }
    }

    public static final class HorseVariantHandler implements VariantHandler<Horse, net.minecraft.world.entity.animal.horse.Variant> {

        @Override
        public List<net.minecraft.world.entity.animal.horse.Variant> getVariants() {
            return Arrays.asList(net.minecraft.world.entity.animal.horse.Variant.values());
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Variant getVariant(Horse entity) {
            return entity.getVariant();
        }

        @Override
        public void setVariant(Horse entity, net.minecraft.world.entity.animal.horse.Variant variant) {
            EntityUtils.setVariantAndMarkings(entity, variant, entity.getMarkings());
        }

        @Override
        public CompoundTag variantToNbt(net.minecraft.world.entity.animal.horse.Variant variant) {
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, variant.name());
            return res;
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Variant nbtToVariant(Tag nbt) {
            return net.minecraft.world.entity.animal.horse.Variant.valueOf(((CompoundTag) nbt).getString(NBT_KEY));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.horse.Variant variant) {
            return variant.getSerializedName().toLowerCase();
        }
    }

    public static final class HorseMarkingsHandler implements VariantHandler<Horse, net.minecraft.world.entity.animal.horse.Markings> {

        @Override
        public List<net.minecraft.world.entity.animal.horse.Markings> getVariants() {
            return Arrays.asList(net.minecraft.world.entity.animal.horse.Markings.values());
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Markings getVariant(Horse entity) {
            return entity.getMarkings();
        }

        @Override
        public void setVariant(Horse entity, net.minecraft.world.entity.animal.horse.Markings variant) {
            EntityUtils.setVariantAndMarkings(entity, entity.getVariant(), variant);
        }

        @Override
        public CompoundTag variantToNbt(net.minecraft.world.entity.animal.horse.Markings variant) {
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, variant.name());
            return res;
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Markings nbtToVariant(Tag nbt) {
            return net.minecraft.world.entity.animal.horse.Markings.valueOf(((CompoundTag) nbt).getString(NBT_KEY));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.horse.Markings variant) {
            return "markings." + variant.name().toLowerCase();
        }
    }

    public static sealed class PandaMainGeneHandler
            implements VariantHandler<Panda, Panda.Gene> permits PandaHiddenGeneHandler {
        @Override
        public boolean isStandard() { return false; }

        @Override
        public List<Panda.Gene> getVariants() {
            return Arrays.asList(Panda.Gene.values());
        }

        @Override
        public Panda.Gene getVariant(Panda entity) {
            return entity.getMainGene();
        }

        @Override
        public void setVariant(Panda entity, Panda.Gene variant) {
            entity.setMainGene(variant);
        }

        @Override
        public CompoundTag variantToNbt(Panda.Gene variant) {
            CompoundTag res = new CompoundTag();
            res.putString(NBT_KEY, variant.name());
            return res;
        }

        @Override
        public Panda.Gene nbtToVariant(Tag nbt) {
            return Enum.valueOf(Panda.Gene.class, ((CompoundTag) nbt).getString(NBT_KEY));
        }

        @Override
        public String getVariantName(Panda.Gene variant) {
            return variant.getSerializedName().toLowerCase();
        }
    }

    public static final class PandaHiddenGeneHandler extends PandaMainGeneHandler {

        @Override
        public Panda.Gene getVariant(Panda entity) {
            return entity.getHiddenGene();
        }

        @Override
        public void setVariant(Panda entity, Panda.Gene variant) {
            entity.setHiddenGene(variant);
        }
    }
}

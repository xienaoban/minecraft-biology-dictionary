package io.github.xienaoban.biologydictionary.core.property.bundle;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.VariantProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class EntityVariantPropertyBundle {
    private static final Bundle<VariantHandler<?, ?>> BUNDLE = new Bundle<>();

    public static void init() {
        register(StandardVariantHandler.PATTERN);

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

    public static <E extends Entity, V> List<VariantHandler<E, V>> getEntries(E entity) {
        return Misc.cast(BUNDLE.getEntries(entity));
    }

    public interface VariantHandler<E extends Entity, V> {
        /**
         * Only the first variant handler in the list checks it.
         */
        default boolean isStandard() { return true; }

        List<V> getVariants();
        V getVariant(E entity);
        void setVariant(E entity, V variant);
        Tag variantToTag(V variant);
        V tagToVariant(Tag tag);
        String getVariantName(V variant);
    }

    public interface PropertyVariantHandler<E extends Entity, V> extends VariantHandler<E, V> {
        AbstractProperty<?, V> createProperty();

        @Override
        default V getVariant(E entity) {
            return createProperty().toValWith(EntityUtils.getNbt(entity));
        }

        @Override
        default void setVariant(E entity, V variant) {
            EntityUtils.mergeNbt(entity, createProperty().toNbtWith(variant));
        }

        @Override
        default Tag variantToTag(V variant) {
            return createProperty().toNbtWith(variant);
        }

        @Override
        default V tagToVariant(Tag tag) {
            return createProperty().toValWith((CompoundTag) tag);
        }
    }

    public record StandardVariantHandler(ResourceKey<Registry<Object>> key, List<Holder<Object>> variants)
            implements EntityVariantPropertyBundle.PropertyVariantHandler<Entity, Holder<Object>> {

        static final Function<Entity, EntityVariantPropertyBundle.VariantHandler<?, ?>> PATTERN = entity -> {
            Class<? extends Entity> entityClass = entity.getClass();
            if (!EntityUtils.isVanillaEntity(entityClass)) { return null; }
            String fullName = EntityUtils.getDeobfuscatedName(entity.getClass());
            String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);

            try {
                Class<?> ofEntity = Class.forName(VanillaEntityProperties.class.getName() + "$Of" + simpleName);
                Method creator = ofEntity.getDeclaredMethod("createVariantProperty");
                @SuppressWarnings("all")
                VariantProperty<Entity, Object> property = (VariantProperty<Entity, Object>) creator.invoke(null);

                ResourceKey<Registry<Object>> key = property.getResourceKey();
                Optional<Registry<Object>> optional = entity.registryAccess().lookup(key);
                if (optional.isEmpty()) { return null; }
                Registry<Object> registry = optional.get();
                List<Holder<Object>> variants = registry.registryKeySet().stream()
                        .map(registry::getOrThrow)
                        .map(k -> (Holder<Object>) k)
                        .toList();

                return new StandardVariantHandler(key, variants);
            } catch (Exception e) {
                LOGGER.debug("Entity `{}` has no variant: {}", entity.getType().toString(), e.toString());
                return null;
            }
        };

        @Override
        public List<Holder<Object>> getVariants() { return variants; }

        @Override
        public String getVariantName(Holder<Object> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                ResourceLocation rl = resourceKey.location();
                String res;
                if (ResourceLocation.DEFAULT_NAMESPACE.equals(rl.getNamespace())) {
                    res = rl.getPath();
                } else {
                    res = rl.getNamespace() + '.' + rl.getPath();
                }
                return res;
            }).orElse("unknown");
        }

        @Override
        public AbstractProperty<Entity, Holder<Object>> createProperty() {
            return new VariantProperty<>(key);
        }
    }

    public static final class VillagerTypeHandler implements EntityVariantPropertyBundle.VariantHandler<Villager, Holder<VillagerType>> {

        @Override
        public boolean isStandard() { return false; }

        @Override
        public List<Holder<VillagerType>> getVariants() {
            return BuiltInRegistries.VILLAGER_TYPE.listElements().map(ref -> (Holder<VillagerType>) ref).toList();
        }

        @Override
        public Holder<VillagerType> getVariant(Villager entity) {
            return entity.getVillagerData().type();
        }

        @Override
        public void setVariant(Villager entity, Holder<VillagerType> variant) {
            entity.setVillagerData(entity.getVillagerData().withType(variant));
        }

        @Override
        public Tag variantToTag(Holder<VillagerType> variant) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE).toNbtWith(variant);
        }

        @Override
        public Holder<VillagerType> tagToVariant(Tag tag) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE).toValWith((CompoundTag) tag);
        }

        @Override
        public String getVariantName(Holder<VillagerType> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                ResourceLocation rl = resourceKey.location();
                String res;
                if (ResourceLocation.DEFAULT_NAMESPACE.equals(rl.getNamespace())) {
                    res = rl.getPath();
                } else {
                    res = rl.getNamespace() + '.' + rl.getPath();
                }
                return res;
            }).orElse("unknown");
        }
    }

    public static final class HorseVariantHandler
            implements EntityVariantPropertyBundle.VariantHandler<Horse, Variant> {

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
        public Tag variantToTag(net.minecraft.world.entity.animal.horse.Variant variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Variant tagToVariant(Tag tag) {
            return net.minecraft.world.entity.animal.horse.Variant.byId(tag.asInt().orElse(0));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.horse.Variant variant) {
            return variant.getSerializedName();
        }
    }

    public static final class HorseMarkingsHandler
            implements EntityVariantPropertyBundle.VariantHandler<Horse, Markings> {

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
        public Tag variantToTag(net.minecraft.world.entity.animal.horse.Markings variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public net.minecraft.world.entity.animal.horse.Markings tagToVariant(Tag tag) {
            return net.minecraft.world.entity.animal.horse.Markings.byId(tag.asInt().orElse(0));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.horse.Markings variant) {
            return "markings." + variant.name().toLowerCase();
        }
    }

    public static sealed class PandaMainGeneHandler implements EntityVariantPropertyBundle.VariantHandler<Panda, Panda.Gene> permits PandaHiddenGeneHandler {
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
        public Tag variantToTag(Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().toNbtWith(variant);
        }

        @Override
        public Panda.Gene tagToVariant(Tag tag) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().toValWith((CompoundTag) tag);
        }

        @Override
        public String getVariantName(Panda.Gene variant) {
            return variant.getSerializedName();
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

        @Override
        public Tag variantToTag(Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().toNbtWith(variant);
        }

        @Override
        public Panda.Gene tagToVariant(Tag tag) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().toValWith((CompoundTag) tag);
        }
    }
}

package io.github.xienaoban.biologydictionary.core.property.bundle;

import com.mojang.serialization.Codec;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.VariantProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class EntityVariantPropertyBundle {
    private static final Bundle<VariantHandler<?, ?>> BUNDLE = new Bundle<>();

    public static void init() {
        register(STANDARD_PATTERN);

        register(EntityType.VILLAGER, new VillagerTypeHandler());
        register(EntityType.HORSE, new HorseVariantHandler(), new HorseMarkingsHandler());
        register(EntityType.PANDA, new PandaMainGeneHandler(), new PandaHiddenGeneHandler());
        register(EntityType.TRADER_LLAMA, new CodecVariantHandler(VanillaEntityProperties.OfLlama.createVariantProperty()));
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
        Tag variantToNbt(V variant);
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
        default Tag variantToNbt(V variant) {
            return createProperty().withVal(variant).toTag();
        }

        @Override
        default V nbtToVariant(Tag nbt) {
            return createProperty().withTag((CompoundTag) nbt).getVal();
        }
    }

    static final Function<Entity, VariantHandler<?, ?>> STANDARD_PATTERN = entity -> {
        Class<? extends Entity> entityClass = entity.getClass();
        if (!EntityUtils.isVanillaEntity(entityClass)) { return null; }
        String fullName = EntityUtils.getDeobfuscatedName(entity.getClass());
        String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);

        for (String variantType : new String[] {"Variant", "Type"}) {
            try {
                Class<?> ofEntity = Class.forName(VanillaEntityProperties.class.getName() + "$Of" + simpleName);
                Method creator = ofEntity.getDeclaredMethod("create" + variantType + "Property");

                if (VariantProperty.class.isAssignableFrom(creator.getReturnType())) {
                    @SuppressWarnings("all")
                    VariantProperty<Entity, Object> property = (VariantProperty<Entity, Object>) creator.invoke(null);

                    ResourceKey<Registry<Object>> key = property.getResourceKey();
                    Optional<Registry<Object>> optional = entity.registryAccess().lookup(key);
                    if (optional.isPresent()) {
                        Registry<Object> registry = optional.get();
                        List<Holder<Object>> variants = registry.registryKeySet().stream()
                                .map(registry::getOrThrow)
                                .map(k -> (Holder<Object>) k)
                                .toList();
                        return new StandardVariantHandler(key, variants);
                    }
                } else if (CodecProperty.class.isAssignableFrom(creator.getReturnType())) {
                    @SuppressWarnings("all")
                    CodecProperty<Entity, Enum<?>> property = (CodecProperty<Entity, Enum<?>>) creator.invoke(null);
                    if (property.getClazz().isEnum()) {
                        return new CodecVariantHandler(property);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Entity `{}` has no variant: {}", entity.getType().toString(), e.toString());
            }
        }
        return null;
    };

    public record StandardVariantHandler(ResourceKey<Registry<Object>> key, List<Holder<Object>> variants)
            implements PropertyVariantHandler<Entity, Holder<Object>> {

        @Override
        public AbstractProperty<Entity, Holder<Object>> createProperty() {
            return new VariantProperty<>(key);
        }

        @Override
        public List<Holder<Object>> getVariants() { return variants; }

        @Override
        public String getVariantName(Holder<Object> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                Identifier id = resourceKey.identifier();
                String res;
                if (Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
                    res = id.getPath();
                } else {
                    res = id.getNamespace() + '.' + id.getPath();
                }
                return res;
            }).orElse("unknown");
        }
    }

    public record CodecVariantHandler(String propertyName, Class<Enum<?>> variantClazz, Codec<Enum<?>> codec)
            implements PropertyVariantHandler<Entity, Enum<?>> {

        public CodecVariantHandler(CodecProperty<? extends Entity, ? extends Enum<?>> property) {
            this(property.name(), Misc.cast(property.getClazz()), Misc.cast(property.getCodec()));
        }

        @Override
        public AbstractProperty<? super Entity, Enum<?>> createProperty() {
            return new CodecProperty<>(propertyName, variantClazz, codec);
        }

        @Override
        public List<Enum<?>> getVariants() {
            return Arrays.asList(variantClazz.getEnumConstants());
        }

        @Override
        public String getVariantName(Enum<?> variant) {
            if (variant instanceof StringRepresentable sr) {
                return sr.getSerializedName();
            } else {
            return variant.name().toLowerCase();
            }
        }
    }

    public static final class VillagerTypeHandler implements VariantHandler<Villager, Holder<VillagerType>> {

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
        public Tag variantToNbt(Holder<VillagerType> variant) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE).withVal(variant).toTag();
        }

        @Override
        public Holder<VillagerType> nbtToVariant(Tag nbt) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE).withTag((CompoundTag) nbt).getVal();
        }

        @Override
        public String getVariantName(Holder<VillagerType> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                Identifier id = resourceKey.identifier();
                String res;
                if (Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
                    res = id.getPath();
                } else {
                    res = id.getNamespace() + '.' + id.getPath();
                }
                return res;
            }).orElse("unknown");
        }
    }

    public static final class HorseVariantHandler implements VariantHandler<Horse, net.minecraft.world.entity.animal.equine.Variant> {

        @Override
        public List<net.minecraft.world.entity.animal.equine.Variant> getVariants() {
            return Arrays.asList(net.minecraft.world.entity.animal.equine.Variant.values());
        }

        @Override
        public net.minecraft.world.entity.animal.equine.Variant getVariant(Horse entity) {
            return entity.getVariant();
        }

        @Override
        public void setVariant(Horse entity, net.minecraft.world.entity.animal.equine.Variant variant) {
            EntityUtils.setVariantAndMarkings(entity, variant, entity.getMarkings());
        }

        @Override
        public Tag variantToNbt(net.minecraft.world.entity.animal.equine.Variant variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public net.minecraft.world.entity.animal.equine.Variant nbtToVariant(Tag nbt) {
            return net.minecraft.world.entity.animal.equine.Variant.byId(nbt.asInt().orElse(0));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.equine.Variant variant) {
            return variant.getSerializedName();
        }
    }

    public static final class HorseMarkingsHandler implements VariantHandler<Horse, net.minecraft.world.entity.animal.equine.Markings> {

        @Override
        public List<net.minecraft.world.entity.animal.equine.Markings> getVariants() {
            return Arrays.asList(net.minecraft.world.entity.animal.equine.Markings.values());
        }

        @Override
        public net.minecraft.world.entity.animal.equine.Markings getVariant(Horse entity) {
            return entity.getMarkings();
        }

        @Override
        public void setVariant(Horse entity, net.minecraft.world.entity.animal.equine.Markings variant) {
            EntityUtils.setVariantAndMarkings(entity, entity.getVariant(), variant);
        }

        @Override
        public Tag variantToNbt(net.minecraft.world.entity.animal.equine.Markings variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public net.minecraft.world.entity.animal.equine.Markings nbtToVariant(Tag nbt) {
            return net.minecraft.world.entity.animal.equine.Markings.byId(nbt.asInt().orElse(0));
        }

        @Override
        public String getVariantName(net.minecraft.world.entity.animal.equine.Markings variant) {
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
        public Tag variantToNbt(Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().withVal(variant).toTag();
        }

        @Override
        public Panda.Gene nbtToVariant(Tag nbt) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().withTag((CompoundTag) nbt).getVal();
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
        public Tag variantToNbt(Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().withVal(variant).toTag();
        }

        @Override
        public Panda.Gene nbtToVariant(Tag nbt) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().withTag((CompoundTag) nbt).getVal();
        }
    }
}

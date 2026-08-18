package io.github.xienaoban.biologydictionary.core.property.bundle;

import com.mojang.serialization.Codec;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.AbstractProperty;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.VariantProperty;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
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

        List<V> getVariants(E entity);
        V getVariant(E entity);
        void setVariant(E entity, V variant);
        Tag variantToNbt(E entity, V variant);
        V nbtToVariant(E entity, Tag nbt);
        String getVariantName(E entity, V variant);
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
        default Tag variantToNbt(E entity, V variant) {
            return createProperty().withVal(variant).toTag();
        }

        @Override
        default V nbtToVariant(E entity, Tag nbt) {
            return createProperty().withTag((CompoundTag) nbt).getVal();
        }
    }

    static final Function<Entity, VariantHandler<?, ?>> STANDARD_PATTERN = new Function<>() {
        @Override
        public VariantHandler<?, ?> apply(Entity entity) {
            Class<? extends Entity> entityClass = entity.getClass();

            try {
                Method getter = findVariantGetter(entityClass);
                if (getter == null) { return null; }

                Method setter = findVariantSetter(entityClass, getter.getReturnType());
                if (setter == null) { return null; }

                MethodHandle getterHandle = MethodHandles.privateLookupIn(
                        getter.getDeclaringClass(), MethodHandles.lookup()).unreflect(getter);
                MethodHandle setterHandle = MethodHandles.privateLookupIn(
                        setter.getDeclaringClass(), MethodHandles.lookup()).unreflect(setter);

                if (getter.getReturnType() == Holder.class) {
                    Class<?> variantClass = getHolderVariantClass(getter);
                    if (variantClass == null) { return null; }

                    ResourceKey<Registry<Object>> key = findRegistryKey(variantClass);
                    if (key == null) { return null; }

                    if (entity.registryAccess().lookup(key).isEmpty()) { return null; }

                    return new HolderVariantHandler(getterHandle, setterHandle, key);
                } else if (getter.getReturnType().isEnum()) {
                    @SuppressWarnings("unchecked")
                    Class<Enum<?>> variantClazz = (Class<Enum<?>>) getter.getReturnType();
                    return new EnumVariantHandler(getterHandle, setterHandle, variantClazz);
                }
            } catch (Exception e) {
                LOGGER.debug("Entity `{}` has no reflective variant: {}", entity.getType().toString(), e.toString());
            }
            return null;
        }

        private Method findVariantGetter(Class<?> entityClass) {
            for (Class<?> clazz = entityClass;
                 clazz != null && Entity.class.isAssignableFrom(clazz);
                 clazz = clazz.getSuperclass()) {
                try {
                    Method method = clazz.getDeclaredMethod("getVariant");
                    if (method.getParameterCount() == 0) {
                        return method;
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
            return null;
        }

        private Method findVariantSetter(Class<?> entityClass, Class<?> variantClass) {
            for (Class<?> clazz = entityClass;
                 clazz != null && Entity.class.isAssignableFrom(clazz);
                 clazz = clazz.getSuperclass()) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.getName().equals("setVariant") || method.getParameterCount() != 1) { continue; }
                    if (method.getParameterTypes()[0].isAssignableFrom(variantClass)) {
                        return method;
                    }
                }
            }
            return null;
        }

        private Class<?> getHolderVariantClass(Method getter) {
            Type returnType = getter.getGenericReturnType();
            if (!(returnType instanceof ParameterizedType parameterizedType)) { return null; }
            if (parameterizedType.getRawType() != Holder.class) { return null; }

            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length != 1 || !(typeArguments[0] instanceof Class<?> variantClass)) { return null; }
            return variantClass;
        }

        @SuppressWarnings("unchecked")
        private ResourceKey<Registry<Object>> findRegistryKey(Class<?> variantClass) throws IllegalAccessException {
            for (Field field : Registries.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.getType() != ResourceKey.class) { continue; }
                if (!isRegistryKeyFor(field.getGenericType(), variantClass)) { continue; }
                return (ResourceKey<Registry<Object>>) field.get(null);
            }
            return null;
        }

        private boolean isRegistryKeyFor(Type type, Class<?> variantClass) {
            if (!(type instanceof ParameterizedType resourceKeyType)
                    || resourceKeyType.getRawType() != ResourceKey.class) {
                return false;
            }

            Type registryType = resourceKeyType.getActualTypeArguments()[0];
            if (!(registryType instanceof ParameterizedType parameterizedRegistry)
                    || parameterizedRegistry.getRawType() != Registry.class) {
                return false;
            }

            Type elementType = parameterizedRegistry.getActualTypeArguments()[0];
            return elementType == variantClass;
        }
    };

    public record HolderVariantHandler(MethodHandle getter, MethodHandle setter, ResourceKey<Registry<Object>> key)
            implements VariantHandler<Entity, Holder<Object>> {

        @Override
        public List<Holder<Object>> getVariants(Entity entity) {
            Optional<Registry<Object>> optional = entity.registryAccess().lookup(key);
            if (optional.isEmpty()) { return List.of(); }

            Registry<Object> registry = optional.get();
            return registry.registryKeySet().stream()
                    .map(registry::getOrThrow)
                    .map(k -> (Holder<Object>) k)
                    .toList();
        }

        @Override
        public Holder<Object> getVariant(Entity entity) {
            try {
                return Misc.cast(getter.invoke(entity));
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to get variant from " + entity.getType(), e);
            }
        }

        @Override
        public void setVariant(Entity entity, Holder<Object> variant) {
            try {
                setter.invoke(entity, variant);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to set variant for " + entity.getType(), e);
            }
        }

        @Override
        public Tag variantToNbt(Entity entity, Holder<Object> variant) {
            TagValueOutput output = TagValueOutput.createWithContext(
                    ProblemReporter.DISCARDING, entity.registryAccess());
            VariantUtils.writeVariant(output, variant);
            return output.buildResult().get(VariantUtils.TAG_VARIANT);
        }

        @Override
        public Holder<Object> nbtToVariant(Entity entity, Tag nbt) {
            CompoundTag inputTag = new CompoundTag();
            inputTag.put(VariantUtils.TAG_VARIANT, nbt);
            TagValueInput input = (TagValueInput) TagValueInput.create(
                    ProblemReporter.DISCARDING, entity.registryAccess(), inputTag);
            return VariantUtils.readVariant(input, key)
                    .map(Misc::<Holder<Object>>cast)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown variant tag for " + entity.getType() + ": " + nbt));
        }

        @Override
        public String getVariantName(Entity entity, Holder<Object> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                Identifier id = resourceKey.identifier();
                String res;
                if (IdentifierUtils.isMc(id)) {
                    res = id.getPath();
                } else {
                    res = id.getNamespace() + '.' + id.getPath();
                }
                return res;
            }).orElse("unknown");
        }
    }

    public record EnumVariantHandler(MethodHandle getter, MethodHandle setter, Class<Enum<?>> variantClazz)
            implements VariantHandler<Entity, Enum<?>> {

        @Override
        public List<Enum<?>> getVariants(Entity entity) {
            return Arrays.asList(variantClazz.getEnumConstants());
        }

        @Override
        public Enum<?> getVariant(Entity entity) {
            try {
                return Misc.cast(getter.invoke(entity));
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to get variant from " + entity.getType(), e);
            }
        }

        @Override
        public void setVariant(Entity entity, Enum<?> variant) {
            try {
                setter.invoke(entity, variant);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to set variant for " + entity.getType(), e);
            }
        }

        @Override
        public Tag variantToNbt(Entity entity, Enum<?> variant) {
            return StringTag.valueOf(variant.name());
        }

        @Override
        public Enum<?> nbtToVariant(Entity entity, Tag nbt) {
            String name = nbt.asString().orElse(null);
            if (name == null) { return null; }

            for (Enum<?> variant : variantClazz.getEnumConstants()) {
                if (variant.name().equals(name)) {
                    return variant;
                }
            }
            return null;
        }

        @Override
        public String getVariantName(Entity entity, Enum<?> variant) {
            if (variant instanceof StringRepresentable sr) {
                return sr.getSerializedName();
            } else {
                return variant.name().toLowerCase();
            }
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
        public List<Enum<?>> getVariants(Entity entity) {
            return Arrays.asList(variantClazz.getEnumConstants());
        }

        @Override
        public String getVariantName(Entity entity, Enum<?> variant) {
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
        public List<Holder<VillagerType>> getVariants(Villager entity) {
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
        public Tag variantToNbt(Villager entity, Holder<VillagerType> variant) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE).withVal(variant).toTag();
        }

        @Override
        public Holder<VillagerType> nbtToVariant(Villager entity, Tag nbt) {
            return new VariantProperty<Horse, VillagerType>(Registries.VILLAGER_TYPE)
                    .withTag((CompoundTag) nbt).getVal();
        }

        @Override
        public String getVariantName(Villager entity, Holder<VillagerType> variant) {
            return variant.unwrapKey().map(resourceKey -> {
                Identifier id = resourceKey.identifier();
                return id.getPath().toLowerCase();
            }).orElse("unknown");
        }
    }

    public static final class HorseVariantHandler implements VariantHandler<Horse, Variant> {

        @Override
        public List<Variant> getVariants(Horse entity) {
            return Arrays.asList(Variant.values());
        }

        @Override
        public Variant getVariant(Horse entity) {
            return entity.getVariant();
        }

        @Override
        public void setVariant(Horse entity, Variant variant) {
            EntityUtils.setVariantAndMarkings(entity, variant, entity.getMarkings());
        }

        @Override
        public Tag variantToNbt(Horse entity, Variant variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public Variant nbtToVariant(Horse entity, Tag nbt) {
            return Variant.byId(nbt.asInt().orElse(0));
        }

        @Override
        public String getVariantName(Horse entity, Variant variant) {
            return variant.getSerializedName();
        }
    }

    public static final class HorseMarkingsHandler implements VariantHandler<Horse, Markings> {

        @Override
        public List<Markings> getVariants(Horse entity) {
            return Arrays.asList(Markings.values());
        }

        @Override
        public Markings getVariant(Horse entity) {
            return entity.getMarkings();
        }

        @Override
        public void setVariant(Horse entity, Markings variant) {
            EntityUtils.setVariantAndMarkings(entity, entity.getVariant(), variant);
        }

        @Override
        public Tag variantToNbt(Horse entity, Markings variant) {
            return IntTag.valueOf(variant.getId());
        }

        @Override
        public Markings nbtToVariant(Horse entity, Tag nbt) {
            return Markings.byId(nbt.asInt().orElse(0));
        }

        @Override
        public String getVariantName(Horse entity, Markings variant) {
            return "markings." + variant.name().toLowerCase();
        }
    }

    public static sealed class PandaMainGeneHandler
            implements VariantHandler<Panda, Panda.Gene> permits PandaHiddenGeneHandler {
        @Override
        public boolean isStandard() { return false; }

        @Override
        public List<Panda.Gene> getVariants(Panda entity) {
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
        public Tag variantToNbt(Panda entity, Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().withVal(variant).toTag();
        }

        @Override
        public Panda.Gene nbtToVariant(Panda entity, Tag nbt) {
            return VanillaEntityProperties.OfPanda.createMainGeneProperty().withTag((CompoundTag) nbt).getVal();
        }

        @Override
        public String getVariantName(Panda entity, Panda.Gene variant) {
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
        public Tag variantToNbt(Panda entity, Panda.Gene variant) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().withVal(variant).toTag();
        }

        @Override
        public Panda.Gene nbtToVariant(Panda entity, Tag nbt) {
            return VanillaEntityProperties.OfPanda.createHiddenGeneProperty().withTag((CompoundTag) nbt).getVal();
        }
    }
}

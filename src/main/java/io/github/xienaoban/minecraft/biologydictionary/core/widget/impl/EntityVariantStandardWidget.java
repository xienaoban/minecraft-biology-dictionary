package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla.VariantProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.UnsupportedWidgetException;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public class EntityVariantStandardWidget extends AbstractEntityVariantWidget<Entity, Holder<Object>> {
    private static final ConcurrentHashMap<Class<? extends Entity>, VariantData> cachedVariantData = new ConcurrentHashMap<>();

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private record VariantData(boolean hit,
                               List<Holder<Object>> variants,
                               ResourceKey<Registry<Object>> key,
                               Supplier<VariantProperty<Entity, Object>> creator,
                               Function<EntityProperties<Entity>, VariantProperty<Entity, Object>> getter) {}

    private static final VariantData NO_VARIANT = new VariantData(false, null, null, null, null);

    private static VariantData getVariantData(Entity entity) {
        return Misc.cast(Objects.requireNonNull(cachedVariantData.get(entity.getClass())));
    }

    private static int getVariantCount(EntityProperties<Entity> properties) {
        return getVariantData(properties.entity()).variants().size();
    }
    
    private static EntityProperties<Entity> verify(EntityProperties<Entity> properties) {
        Entity entity = properties.entity();
        VariantData vd = cachedVariantData.computeIfAbsent(entity.getClass(), entityClazz -> {
            if (!EntityUtils.isVanillaEntity(entity)) {
                return NO_VARIANT;
            }
            String fullName = EntityUtils.getDeobfuscatedName(entity.getClass());
            String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
            try {
                Class<?> ofEntity = Class.forName(EntityVanillaProperties.class.getName() + "$Of" + simpleName);
                Method creatorM = ofEntity.getDeclaredMethod("createVariantProperty");
                Method getterM = ofEntity.getDeclaredMethod("getVariantProperty", EntityProperties.class);
                if (creatorM.getReturnType() != VariantProperty.class
                        || getterM.getReturnType() != VariantProperty.class) {
                    return NO_VARIANT;
                }
                final MethodHandle creatorMH = lookup.unreflect(creatorM);
                final MethodHandle getterMH = lookup.unreflect(getterM);

                Supplier<VariantProperty<Entity, Object>> creator = () -> {
                    try {
                        @SuppressWarnings("all")
                        VariantProperty<Entity, Object> res = (VariantProperty<Entity, Object>) creatorMH.invokeExact();
                        return res;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };

                Function<EntityProperties<Entity>, VariantProperty<Entity, Object>> getter = properties1 -> {
                    try {
                        @SuppressWarnings("all")
                        VariantProperty<Entity, Object> res = (VariantProperty<Entity, Object>) getterMH.invokeExact(properties1);
                        return res;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };

                ResourceKey<Registry<Object>> key = creator.get().getResourceKey();
                Optional<Registry<Object>> optional = entity.registryAccess().lookup(key);
                if (optional.isEmpty()) { return NO_VARIANT; }
                Registry<Object> registry = optional.get();
                List<Holder<Object>> variants = registry.registryKeySet().stream()
                        .map(registry::getOrThrow)
                        .map(k -> (Holder<Object>) k)
                        .toList();

                return new VariantData(true, variants, key, creator, getter);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                LOGGER.debug("Entity `{}` has no variant: {}", entity.getType().toString(), e.toString());
                return NO_VARIANT;
            }
        });
        UnsupportedWidgetException.verify(vd.hit());
        return properties;
    }

    public EntityVariantStandardWidget(EntityProperties<Entity> properties) {
        super(verify(properties), getVariantCount(properties));
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<Holder<Object>> getAllVariants() {
        return getVariantData(e()).variants();
    }

    @Override
    protected Holder<Object> getVariantClient(Entity entity) {
        VariantData variantData = getVariantData(e());
        CompoundTag nbt = EntityUtils.getNbt(entity);
        VariantProperty<Entity, Object> property = new VariantProperty<>(variantData.key());
        property.readFrom(nbt);
        return property.get();
    }

    @Override
    protected void setVariantClient(Entity entity, Holder<Object> variant) {
        VariantData variantData = getVariantData(e());
        VariantProperty<Entity, Object> property = new VariantProperty<>(variantData.key());
        property.set(variant);
        EntityUtils.mergeNbt(entity, property.toNbt());
    }

    @Override
    protected Component getVariantName(Holder<Object> variant) {
        String name = variant.unwrapKey().map(resourceKey -> {
            ResourceLocation rl = resourceKey.location();
            String res;
            if (ResourceLocation.DEFAULT_NAMESPACE.equals(rl.getNamespace())) {
                res = rl.getPath();
            } else {
                res = rl.getNamespace() + '.' + rl.getPath();
            }
            return res;
        }).orElse("unknown");
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        VariantData variantData = getVariantData(e());
        VariantProperty<Entity, Object> property = new VariantProperty<>(variantData.key());
        property.set(element.getVariant());
        property.writeTo(vanillaNbt);
    }
}

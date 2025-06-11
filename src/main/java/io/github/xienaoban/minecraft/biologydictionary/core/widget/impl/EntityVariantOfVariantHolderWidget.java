package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.UnsupportedWidgetException;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.VariantHolder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class EntityVariantOfVariantHolderWidget extends AbstractEntityVariantWidget<Entity, Object> {
    private static final ConcurrentHashMap<Class<? extends Entity>, VariantData> cachedVariantData = new ConcurrentHashMap<>();

    private record VariantData(boolean hit, Class<?> variantClazz, String variantType, List<Object> variants,
                               Function<Object, String> variantName, List<String> variantNbtKeys) {}

    private static final VariantData NO_VARIANT = new VariantData(false, null, null, null, null, null);

    private static VariantData getVariantData(Entity entity) {
        return Objects.requireNonNull(cachedVariantData.get(entity.getClass()));
    }

    private static int getVariantCount(EntityProperties<Entity> properties) {
        return getVariantData(properties.entity()).variants().size();
    }

    private static EntityProperties<Entity> verify(EntityProperties<Entity> properties) {
        Entity entity = properties.entity();
        VariantData vd = cachedVariantData.computeIfAbsent(entity.getClass(), entityClazz -> {
            // 1. Find interface VariantHolder.
            for (Class<?> c : EntityUtils.bottomUp(entityClazz)) {
                for (Type t : c.getGenericInterfaces()) {
                    if (!(t instanceof ParameterizedType pt)) {
                        continue;
                    }
                    if (pt.getRawType() != VariantHolder.class) {
                        continue;
                    }

                    // 2. Get variant class.
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length != 1) {
                        break;
                    }
                    Type type = args[0];

                    // 3. Handle different types of variant.
                    if (type instanceof Class<?> variantClazz && variantClazz.isEnum()) {
                        return createEnumVariantData(entity, variantClazz);
                    } else if (type instanceof ParameterizedType variantPT
                            && variantPT.getRawType() instanceof Class<?> variantClazz
                            && Holder.class.isAssignableFrom(variantClazz)) {
                        return createHolderVariantData(entity, variantClazz);
                    }
                    break;
                }
            }
            return NO_VARIANT;
        });
        UnsupportedWidgetException.verify(vd.hit());
        return properties;
    }

    private static VariantData createEnumVariantData(Entity entity, Class<?> variantClazz) {
        List<Object> variants = Misc.cast(Arrays.stream(variantClazz.getEnumConstants()).toList());
        return new VariantData(true, variantClazz, "enum", variants, (variant) -> {
            String name;
            if (variant instanceof StringRepresentable sr) {
                name = sr.getSerializedName();
            } else {
                name = ((Enum<?>) variant).name().toLowerCase();
            }
            return name;
        }, calcVariantNbtKeys(entity, variants));
    }

    private static VariantData createHolderVariantData(Entity entity, Class<?> variantClazz) {
        String variantName = EntityUtils.getEntityTypeName(entity).getPath() + "_variant";
        Registry<Object> registry = Misc.cast(BuiltInRegistries.REGISTRY
                .get(ResourceLocation.withDefaultNamespace(variantName))
                .orElseThrow(UnsupportedWidgetException::get)
                .value());
        List<Object> variants = registry.registryKeySet().stream()
                .map(registry::getOrThrow)
                .map(k -> (Object) k)
                .toList();

        return new VariantData(true, variantClazz, "holder" , variants, (variant) -> {
            String[] kv = ((Holder<?>) variant).getRegisteredName().split(":");
            String k = kv[0], v = kv[1];
            String res;
            if (ResourceLocation.DEFAULT_NAMESPACE.equals(k)) {
                res = v;
            } else {
                res = k + '.' + v;
            }
            return res;
        }, calcVariantNbtKeys(entity, variants));
    }

    /**
     * Set variant twice and see what's the difference between two exported NBT tags.
     * And the differences are about the variants.
     */
    private static List<String> calcVariantNbtKeys(Entity entity, List<Object> variants) {
        Entity tmp = EntityUtils.create(EntityUtils.getEntityType(entity));
        VariantHolder<Object> vh = Misc.cast(tmp);
        List<String> res = new ArrayList<>();

        Object v1 = variants.get(0), v2 = variants.get(1);
        CompoundTag t1 = new CompoundTag(), t2 = new CompoundTag();
        Set<String> keys = new HashSet<>();
        vh.setVariant(v1);
        tmp.saveWithoutId(t1);
        keys.addAll(t1.getAllKeys());
        vh.setVariant(v2);
        tmp.saveWithoutId(t2);
        keys.addAll(t2.getAllKeys());
        for (String key : keys) {
            if (Objects.equals(t1.get(key), t2.get(key))) {
                continue;
            }
            res.add(key);
        }
        return res;
    }

    public EntityVariantOfVariantHolderWidget(EntityProperties<Entity> properties) {
        super(verify(properties), getVariantCount(properties));
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<Object> getAllVariants() {
        return getVariantData(e()).variants();
    }

    @Override
    protected Object getVariantClient(Entity entity) {
        return ((VariantHolder<?>) entity).getVariant();
    }

    @Override
    protected void setVariantClient(Entity entity, Object variant) {
        ((VariantHolder<?>) entity).setVariant(Misc.cast(variant));
    }

    @Override
    protected Component getVariantName(Object variant) {
        String name = getVariantData(e()).variantName.apply(variant);
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        Entity model = element.getModel();
        CompoundTag nbt = new CompoundTag();
        model.saveWithoutId(nbt);
        for (String key : getVariantData(e()).variantNbtKeys()) {
            vanillaNbt.put(key, Objects.requireNonNull(nbt.get(key)));
        }
    }
}

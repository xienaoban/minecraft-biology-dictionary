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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class EntityVariantOfEnumWidget extends AbstractEntityVariantWidget<Entity, Object> {
    private static final ConcurrentHashMap<Class<? extends Entity>, VariantData> cachedVariantData = new ConcurrentHashMap<>();

    private interface VariantData {
        boolean hit();
        Class<?> variantClazz();
        String variantType();
        List<Object> variants();
        String variantName(Object variant);
        void variantToNbt(Object variant, CompoundTag vanillaNbt, CompoundTag extraNbt);
    }

    private static final VariantData NO_VARIANT = new VariantData() {
        @Override public boolean hit() { return false; }
        @Override public Class<?> variantClazz() { return null; }
        @Override public String variantType() { return null; }
        @Override public List<Object> variants() { return null; }
        @Override public String variantName(Object variant) { return null; }
        @Override public void variantToNbt(Object variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {}
    };

    private static VariantData createEnumVariantData(Entity entity, Class<?> variantClazz) {
        List<Object> variants = Misc.cast(Arrays.stream(variantClazz.getEnumConstants()).toList());
        return new VariantData() {
            @Override public boolean hit() { return true; }
            @Override public Class<?> variantClazz() { return variantClazz; }
            @Override public String variantType() { return "enum"; }
            @Override public List<Object> variants() { return variants; }

            @Override public String variantName(Object variant) {
                String name;
                if (variant instanceof StringRepresentable sr) {
                    name = sr.getSerializedName();
                } else {
                    name = ((Enum<?>) variant).name().toLowerCase();
                }
                return name;
            }

            @Override public void variantToNbt(Object variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {}
        };
    }

    private static VariantData createHolderVariantData(Entity entity, Class<?> variantClazz) {
        String variantName = EntityUtils.getEntityTypeName(entity).getPath() + "_variant";
        Registry<?> registryTmp = BuiltInRegistries.REGISTRY
                .get(ResourceLocation.withDefaultNamespace(variantName))
                .orElseThrow(UnsupportedWidgetException::get)
                .value();
        Registry<Object> registry = Misc.cast(registryTmp);
        List<Object> variants = registry.registryKeySet().stream()
                .map(registry::getOrThrow)
                .map(k -> (Object) k)
                .toList();
        return new VariantData() {
            @Override public boolean hit() { return true; }
            @Override public Class<?> variantClazz() { return variantClazz; }
            @Override public String variantType() { return "holder"; }
            @Override public List<Object> variants() { return variants; }

            @Override public String variantName(Object variant) {
                String[] kv = ((Holder<?>) variant).getRegisteredName().split(":");
                String k = kv[0], v = kv[1];
                String res;
                if (ResourceLocation.DEFAULT_NAMESPACE.equals(k)) {
                    res = v;
                } else {
                    res = k + '.' + v;
                }
                return res;
            }

            @Override public void variantToNbt(Object variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {}
        };
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

    private static VariantData getVariantData(Entity entity) {
        return Objects.requireNonNull(cachedVariantData.get(entity.getClass()));
    }

    private static int getVariantCount(EntityProperties<Entity> properties) {
        return cachedVariantData.get(properties.entity().getClass()).variants().size();
    }

    public EntityVariantOfEnumWidget(EntityProperties<Entity> properties) {
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
        String name = getVariantData(e()).variantName(variant);
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected void writeVariantToNbt(Object variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {

    }
}

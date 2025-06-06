package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.UnsupportedWidgetException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.VariantHolder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class EntityVariantWithNameWidget extends AbstractEntityVariantWidget<Entity, Object> {
    private static final ConcurrentHashMap<Class<? extends Entity>, VariantData> cachedVariantData = new ConcurrentHashMap<>();

    private record VariantData(boolean hit, Class<?> variantClazz, List<Object> variants) {}

    private static EntityProperties<Entity> verify(EntityProperties<Entity> properties) {
        VariantData vd = cachedVariantData.computeIfAbsent(properties.entity().getClass(), entityClazz -> {
            for (Type t : entityClazz.getGenericInterfaces()) {
                if (!(t instanceof ParameterizedType pt)) {
                    continue;
                }
                if (pt.getRawType() != VariantHolder.class) {
                    continue;
                }

                Type[] args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] instanceof Class<?> variantClazz && variantClazz.isEnum()) {
                    List<Object> variants = Misc.cast(Arrays.stream(variantClazz.getEnumConstants()).toList());
                    return new VariantData(true, variantClazz, variants);
                }
                break;
            }
            return new VariantData(false, null, null);
        });
        UnsupportedWidgetException.verify(vd.hit());
        return properties;
    }

    private static int getVariantCount(EntityProperties<Entity> properties) {
        return cachedVariantData.get(properties.entity().getClass()).variants().size();
    }

    public EntityVariantWithNameWidget(EntityProperties<Entity> properties) {
        super(verify(properties), getVariantCount(properties), 7, 2);
    }

    @Override
    protected List<Object> getAllVariants() {
        return cachedVariantData.get(e().getClass()).variants();
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
        String name;
        if (variant instanceof StringRepresentable sr) {
            name = sr.getSerializedName();
        } else {
            name = ((Enum<?>) variant).name().toLowerCase();
        }
        return Component.translatable(getVariantNameKeyPrefix() + name);
    }

    @Override
    protected void writeVariantToNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {

    }

    @Override
    protected Object readVariantFromNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        return null;
    }
}

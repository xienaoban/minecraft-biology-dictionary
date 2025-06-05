package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class HorseVariantWidget extends AbstractEntityVariantWidget<Horse, Variant> {
    private static final List<Variant> variants = Arrays.stream(Variant.values()).sorted(Comparator.comparingInt(Variant::getId)).toList();

    public HorseVariantWidget(EntityProperties<Horse> properties) {
        super(properties, variants.size(), 7, 2);
    }

    @Override
    protected List<Variant> getAllVariants() {
        return variants;
    }

    @Override
    protected Variant getVariantClient(Horse entity) {
        return entity.getVariant();
    }

    @Override
    protected void setVariantClient(Horse entity, Variant variant) {
        entity.setVariant(variant);
    }

    @Override
    protected Component getVariantName(Variant variant) {
        return Component.translatable(variant.getSerializedName());
    }

    @Override
    protected void writeVariantToNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        throw new RuntimeException();
    }

    @Override
    protected Variant readVariantFromNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        throw new RuntimeException();
    }
}

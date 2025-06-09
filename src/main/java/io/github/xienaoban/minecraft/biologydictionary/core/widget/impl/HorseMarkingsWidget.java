package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.mixin.HorseIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class HorseMarkingsWidget extends AbstractEntityVariantWidget<Horse, Markings> {
    private static final List<Markings> markings = Arrays.stream(Markings.values()).sorted(Comparator.comparingInt(Markings::getId)).toList();

    public HorseMarkingsWidget(EntityProperties<Horse> properties) {
        super(properties, markings.size(), 2);
        setBackgroundBars(Textures.ICONS, 3 * Widget.WIDGET_WIDTH, 24 * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<Markings> getAllVariants() {
        return markings;
    }

    @Override
    protected Markings getVariantClient(Horse entity) {
        return entity.getMarkings();
    }

    @Override
    protected void setVariantClient(Horse entity, Markings variant) {
        ((HorseIMixin) entity).invokeSetVariantAndMarkings(e().getVariant(), variant);
    }

    @Override
    protected Component getVariantName(Markings variant) {
        return Component.translatable( getVariantNameKeyPrefix() + "markings." + variant.name().toLowerCase());
    }

    @Override
    protected void writeVariantToNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        throw new RuntimeException();
    }

    @Override
    protected Markings readVariantFromNbt(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        throw new RuntimeException();
    }
}

package io.github.xienaoban.biologydictionary.core.widget.impl;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class HorseMarkingsWidget extends AbstractEntityVariantWidget<Horse, Markings> {
    private static final List<Markings> markings = Arrays.stream(Markings.values()).sorted(Comparator.comparingInt(Markings::getId)).toList();

    public HorseMarkingsWidget(EntityProperties<Horse> properties) {
        super(properties, markings.size());
        setBackgroundBars(Textures.ICONS, BG_BAR2_LEFT * Widget.WIDGET_WIDTH, BG_BAR2_TOP * Widget.WIDGET_HEIGHT);
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
        EntityUtils.setVariantAndMarkings(entity, e().getVariant(), variant);
    }

    @Override
    protected Component getVariantName(Markings variant) {
        return Component.translatable( getVariantNameKeyPrefix() + "markings." + variant.name().toLowerCase());
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        Horse tmp = EntityUtils.create(e());
        EntityUtils.setVariantAndMarkings(tmp, e().getVariant(), element.getVariant());
        CompoundTag nbt = EntityUtils.getNbt(tmp);
        String key = EntityVanillaProperties.OfHorse.getVariantProperty(p()).name();
        vanillaNbt.put(key, Objects.requireNonNull(nbt.get(key)));
    }
}

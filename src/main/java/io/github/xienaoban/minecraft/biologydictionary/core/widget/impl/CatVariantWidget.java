package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.property.StringProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.CatVariant;

import java.util.List;

public class CatVariantWidget extends AbstractEntityVariantWidget<Cat, Holder<CatVariant>> {
    private static final Registry<CatVariant> registry = BuiltInRegistries.CAT_VARIANT;
    private static final List<Holder<CatVariant>> variants = registry.registryKeySet().stream().map(
            k -> (Holder<CatVariant>) registry.getOrThrow(k)).toList();

    public CatVariantWidget(EntityProperties<Cat> properties) {
        super(properties, variants.size());
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
        setVariantElementWidthFix(2);
        setVariantElementHeightFix(3);
    }

    @Override
    protected List<Holder<CatVariant>> getAllVariants() {
        return variants;
    }

    @Override
    protected Holder<CatVariant> getVariantClient(Cat entity) {
        return entity.getVariant();
    }

    @Override
    protected void setVariantClient(Cat entity, Holder<CatVariant> variant) {
        entity.setVariant(variant);
    }

    @Override
    protected Component getVariantName(Holder<CatVariant> variant) {
        String[] kv = variant.getRegisteredName().split(":");
        String k = kv[0], v = kv[1];
        String res;
        if (ResourceLocation.DEFAULT_NAMESPACE.equals(k)) {
            res = v;
        } else {
            res = k + '.' + v;
        }
        return Component.translatable(Lang.VARIANT_CAT_PREFIX + res);
    }

    @Override
    protected void writeVariantToNbt(Holder<CatVariant> variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        StringProperty<Cat> property = EntityVanillaProperties.OfCat.createVariantProperty();
        property.set(variant.unwrapKey().orElseThrow().location().toString());
        property.writeTo(vanillaNbt);
    }
}

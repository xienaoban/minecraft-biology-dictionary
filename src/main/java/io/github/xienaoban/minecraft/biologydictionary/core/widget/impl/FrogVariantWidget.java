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
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;

import java.util.List;

public class FrogVariantWidget extends AbstractEntityVariantWidget<Frog, Holder<FrogVariant>> {
    private static final Registry<FrogVariant> registry = BuiltInRegistries.FROG_VARIANT;
    private static final List<Holder<FrogVariant>> variants = registry.registryKeySet().stream().map(
            k -> (Holder<FrogVariant>) registry.getOrThrow(k)).toList();

    public FrogVariantWidget(EntityProperties<Frog> properties) {
        super(properties, variants.size());
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
        setVariantElementWidthFix(-6);
    }

    @Override
    protected List<Holder<FrogVariant>> getAllVariants() {
        return variants;
    }

    @Override
    protected Holder<FrogVariant> getVariantClient(Frog entity) {
        return entity.getVariant();
    }

    @Override
    protected void setVariantClient(Frog entity, Holder<FrogVariant> variant) {
        entity.setVariant(variant);
    }

    @Override
    protected Component getVariantName(Holder<FrogVariant> variant) {
        String[] kv = variant.getRegisteredName().split(":");
        String k = kv[0], v = kv[1];
        String res;
        if (ResourceLocation.DEFAULT_NAMESPACE.equals(k)) {
            res = v;
        } else {
            res = k + '.' + v;
        }
        return Component.translatable(Lang.VARIANT_FROG_PREFIX + res);
    }

    @Override
    protected void writeVariantToNbt(Holder<FrogVariant> variant, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        StringProperty<Frog> property = EntityVanillaProperties.OfFrog.createVariantProperty();
        property.set(variant.unwrapKey().orElseThrow().location().toString());
        property.writeTo(vanillaNbt);
    }
}

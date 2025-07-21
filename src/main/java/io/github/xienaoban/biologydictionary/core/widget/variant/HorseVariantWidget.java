package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class HorseVariantWidget extends AbstractEntityVariantWidget<Horse, Variant> {
    private static final List<Variant> variants = Arrays.stream(Variant.values()).sorted(Comparator.comparingInt(Variant::getId)).toList();

    public HorseVariantWidget(EntityProperties<Horse> properties) {
        super(properties, variants.size());
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
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
        EntityUtils.setVariantAndMarkings(entity, variant, entity.getMarkings());
    }

    @Override
    protected Component getVariantName(Variant variant) {
        return Component.translatable( getVariantNameKeyPrefix() + variant.name().toLowerCase());
    }

    @Override
    protected void writeVariantToNbt(VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        Horse tmp = EntityUtils.create(e());
        EntityUtils.setVariantAndMarkings(tmp, element.getVariant(), e().getMarkings());
        CompoundTag nbt = EntityUtils.getNbt(tmp);
        String key = VanillaEntityProperties.OfHorse.getVariantProperty(p()).name();
        vanillaNbt.put(key, Objects.requireNonNull(nbt.get(key)));
    }
}

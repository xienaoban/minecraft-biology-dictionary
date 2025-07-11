package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Panda;

import java.util.Arrays;
import java.util.List;

public class PandaMainGeneWidget extends AbstractEntityVariantWidget<Panda, Panda.Gene> {
    private static final List<Panda.Gene> genes = Arrays.stream(Panda.Gene.values()).toList();

    public PandaMainGeneWidget(EntityProperties<Panda> properties) {
        super(properties, genes.size());
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected List<Panda.Gene> getAllVariants() {
        return genes;
    }

    @Override
    protected Panda.Gene getVariantClient(Panda entity) {
        return entity.getMainGene();
    }

    @Override
    protected void setVariantClient(Panda entity, Panda.Gene variant) {
        entity.setMainGene(variant);
    }

    @Override
    protected Component getVariantName(Panda.Gene variant) {
        return Component.translatable(getVariantNameKeyPrefix() + variant.getSerializedName());
    }

    @Override
    protected void writeVariantToNbt(AbstractEntityVariantWidget<Panda, Panda.Gene>.VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        CodecProperty<Panda, Panda.Gene> gene = EntityVanillaProperties.OfPanda.createMainGeneProperty();
        gene.set(element.getVariant());
        gene.writeTo(vanillaNbt);
    }
}

package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Panda;

public class PandaHiddenGeneWidget extends PandaMainGeneWidget {
    public PandaHiddenGeneWidget(EntityProperties<Panda> properties) {
        super(properties);
        setBackgroundBars(Textures.ICONS, BG_BAR2_LEFT * Widget.WIDGET_WIDTH, BG_BAR2_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected Panda.Gene getVariantClient(Panda entity) {
        return entity.getHiddenGene();
    }

    @Override
    protected void setVariantClient(Panda entity, Panda.Gene variant) {
        entity.setHiddenGene(variant);
    }

    @Override
    protected void writeVariantToNbt(AbstractEntityVariantWidget<Panda, Panda.Gene>.VariantElement element, CompoundTag vanillaNbt, CompoundTag extraNbt) {
        CodecProperty<Panda, Panda.Gene> gene = VanillaEntityProperties.OfPanda.createHiddenGeneProperty();
        gene.set(element.getVariant());
        gene.writeTo(vanillaNbt);
    }
}

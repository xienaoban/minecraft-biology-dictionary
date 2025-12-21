package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.animal.panda.Panda;

public sealed class PandaMainGeneWidget extends AbstractEntityStandardVariantWidget<Panda, Panda.Gene> permits PandaHiddenGeneWidget {
    public static final Factory<Panda> FACTORY = PandaMainGeneWidget::new;

    private static final int VH_IDX = 0;

    public PandaMainGeneWidget(EntityProperties<Panda> properties) {
        super(properties, getVariantCount(properties, VH_IDX));
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    protected PandaMainGeneWidget(EntityProperties<Panda> properties, int variantCnt) {
        super(properties, variantCnt);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_MAIN_GENE),
                tooltipDescription(Lang.PROPERTY_WIDGET_MAIN_GENE_DESC)
        );
        return true;
    }
}

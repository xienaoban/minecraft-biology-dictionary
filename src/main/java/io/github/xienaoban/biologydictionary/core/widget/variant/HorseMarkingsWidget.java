package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;

@Environment(EnvType.CLIENT)
public final class HorseMarkingsWidget extends AbstractEntityStandardVariantWidget<Horse, Markings> {
    private static final int VH_IDX = 1;

    public HorseMarkingsWidget(EntityProperties<Horse> properties) {
        super(properties, getVariantCount(properties, VH_IDX));
        setBackgroundBars(Textures.ICONS, BG_BAR2_LEFT * Widget.WIDGET_WIDTH, BG_BAR2_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_MARKINGS),
                tooltipDescription(Lang.PROPERTY_WIDGET_MARKINGS_DESC)
        );
        return true;
    }
}

package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;

public final class VillagerTypeWidget extends AbstractEntityStandardVariantWidget<Villager, Holder<VillagerType>> {
    private static final int VH_IDX = 0;

    public VillagerTypeWidget(EntityProperties<Villager> properties) {
        super(properties, getVariantCount(properties, VH_IDX), 7, 3);
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_VILLAGER_TYPE),
                tooltipDescription(Lang.PROPERTY_WIDGET_VILLAGER_TYPE_DESC)
        );
        return true;
    }
}

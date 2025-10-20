package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.Entity;

public class EntityStandardVariantWidget extends AbstractEntityStandardVariantWidget<Entity, Object> {
    private static final int VH_IDX = 0;

    public EntityStandardVariantWidget(EntityProperties<Entity> properties) {
        super(verify(properties, VH_IDX), getVariantCount(properties, VH_IDX));
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }
}

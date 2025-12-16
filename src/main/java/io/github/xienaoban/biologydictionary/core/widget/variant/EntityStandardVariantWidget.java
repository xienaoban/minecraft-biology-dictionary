package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class EntityStandardVariantWidget extends AbstractEntityStandardVariantWidget<Entity, Object> {
    private static final int VH_IDX = 0;

    public static final Factory<Entity> FACTORY = properties -> {
        Entity entity = properties.entity();
        List<EntityVariantPropertyBundle.VariantHandler<Entity, Object>> list
                = EntityVariantPropertyBundle.getHandlers(entity);
        if (list.size() > VH_IDX && list.get(VH_IDX).isStandard()) {
            return new EntityStandardVariantWidget(properties);
        }
        return null;
    };

    public EntityStandardVariantWidget(EntityProperties<Entity> properties) {
        super(properties, getVariantCount(properties, VH_IDX));
        setBackgroundBars(Textures.ICONS, BG_BAR1_LEFT * Widget.WIDGET_WIDTH, BG_BAR1_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }
}

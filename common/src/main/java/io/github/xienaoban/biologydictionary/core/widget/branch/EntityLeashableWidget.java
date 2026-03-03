package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;

@Environment(EnvType.CLIENT)
public final class EntityLeashableWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityLeashableWidget::new;

    private static final int L = 17, T = 1;

    private final boolean leashable;

    public EntityLeashableWidget(EntityProperties<Entity> properties) {
        super(properties, Page.COLUMNS / 4);
        if (e() instanceof Leashable entity) {
            leashable = (entity.isLeashed() || entity.canBeLeashed());
        } else {
            leashable = false;
        }

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new LeashableButton());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_LEASHABLE),
                tooltipDescription(Lang.PROPERTY_WIDGET_LEASHABLE_DESC)
        );
        return true;
    }

    private final class LeashableButton extends EntityPropertyButton {

        public LeashableButton() {
            super(Textures.ICONS, L_YES_NO * WIDGET_WIDTH, T_YES_NO * WIDGET_HEIGHT);
            setSelectable(false);
            setTextureLeftOffset((leashable ? 0 : 1) * WIDGET_WIDTH);
        }
    }
}

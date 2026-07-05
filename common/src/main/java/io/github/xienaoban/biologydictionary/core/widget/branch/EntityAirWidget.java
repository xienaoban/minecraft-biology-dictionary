package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.world.entity.Entity;

@ClientOnly
public final class EntityAirWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityAirWidget::new;

    private static final int L = 6, T = 1;

    public EntityAirWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new AirBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_AIR),
                tooltipDescription(Lang.PROPERTY_WIDGET_AIR_DESC)
        );
        return true;
    }

    private final class AirBar extends EntityPropertyProgressBar {
        public AirBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            updatePercent((float) e().getAirSupply() / (float) e().getMaxAirSupply());
            super.onRender(ctx);
            if (BiologyDictionaryClient.isDebugMode()) {
                renderInnerText(ctx, TextUtils.literal(e().getAirSupply() + "t/" + e().getMaxAirSupply() + "t"));
            } else {
                renderInnerText(ctx, TextUtils.literal(
                        (e().getAirSupply() / ClientUtils.getClientTickCountPerSecond())
                                + "s/"
                                + (e().getMaxAirSupply() / ClientUtils.getClientTickCountPerSecond()) + "s"));
            }
        }
    }
}

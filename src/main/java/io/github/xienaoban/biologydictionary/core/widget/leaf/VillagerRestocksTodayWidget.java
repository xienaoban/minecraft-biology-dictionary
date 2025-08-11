package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;

public class VillagerRestocksTodayWidget extends EntityPropertyStandardWidget<Villager> {
    private static final int L = 6, T = 5;

    private static final int MAX_RESTOCK_TODAY = 2;

    private final IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.getRestocksTodayProperty(p());

    public VillagerRestocksTodayWidget(EntityProperties<Villager> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new RestocksTodayBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_RESTOCKS_TODAY),
                tooltipDescription(Lang.PROPERTY_WIDGET_RESTOCKS_TODAY_DESC)
        );
        return true;
    }

    private final class RestocksTodayBar extends EntityPropertyProgressBar {

        public RestocksTodayBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer numI = restocksTodayProperty.get();
            if (numI == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }

            int num = numI;
            updatePercent((float) num / MAX_RESTOCK_TODAY);
            super.onRender(ctx);
            renderInnerText(ctx, Component.literal(String.valueOf(num)));
        }
    }
}

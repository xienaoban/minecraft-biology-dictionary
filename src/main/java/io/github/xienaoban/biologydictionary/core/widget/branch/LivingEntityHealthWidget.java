package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public final class LivingEntityHealthWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityHealthWidget::new;

    private static final int L = 1, T = 1;

    public LivingEntityHealthWidget(EntityProperties<LivingEntity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new HealthBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_HEALTH),
                tooltipDescription(Lang.PROPERTY_WIDGET_HEALTH_DESC)
        );
        return true;
    }

    private final class HealthBar extends EntityPropertyProgressBar {
        public HealthBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            updatePercent(e().getHealth() / e().getMaxHealth());
            super.onRender(ctx);
            renderInnerText(ctx, Component.literal(((int) e().getHealth()) + "/" + ((int) e().getMaxHealth())));
        }
    }
}

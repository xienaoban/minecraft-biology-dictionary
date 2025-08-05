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
import net.minecraft.world.entity.animal.Dolphin;

public class DolphinMoistnessWidget extends EntityPropertyStandardWidget<Dolphin> {
    private static final int L = 6, T = 2;

    /**
     * @see Dolphin#TOTAL_MOISTNESS_LEVEL
     */
    private static final int TOTAL_MOISTNESS_LEVEL = 2400;

    private final IntProperty<Dolphin> moistnessProperty = VanillaEntityProperties.OfDolphin.getMoistnessProperty(p());

    public DolphinMoistnessWidget(EntityProperties<Dolphin> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new MoistnessBar());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer mL = moistnessProperty.get();
        if (mL == null) {
            return;
        }
        int m = mL;
        if (m > 0 && !e().isInWaterOrRain() && !e().isNoAi()) {
            moistnessProperty.set(m - 1);
        }
    }

    private final class MoistnessBar extends EntityPropertyProgressBar {

        public MoistnessBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer mL = moistnessProperty.get();
            if (mL == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }
            int m = mL;
            int mMax = TOTAL_MOISTNESS_LEVEL;
            updatePercent((float) m / mMax);
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(m + "t/" + mMax + "t"));
            } else {
                renderInnerText(ctx, Component.literal((m / 20) + "s/" + (mMax / 20) + "s"));
            }
        }
    }
}

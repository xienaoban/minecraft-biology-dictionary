package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.Animal;

@Environment(EnvType.CLIENT)
public final class AnimalInLoveWidget extends EntityPropertyStandardWidget<Animal> {
    public static final Factory<Animal> FACTORY = AnimalInLoveWidget::new;

    private static final int L = 6, T = 3;

    private static final int IN_LOVE_MAX_TIME = 600;

    private final IntProperty<Animal> inLoveProperty = VanillaEntityProperties.OfAnimal.getInLoveProperty(p());

    public AnimalInLoveWidget(EntityProperties<Animal> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new InLoveBar());
    }

    private boolean isBabyClient() {
        return EntityUtils.isBaby(e());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer inLoveOpt = inLoveProperty.getVal();
        if (inLoveOpt == null) {
            return;
        }
        int age = inLoveOpt;
        if (age > 0) {
            inLoveProperty.setVal(age - 1);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_IN_LOVE),
                tooltipDescription(Lang.PROPERTY_WIDGET_IN_LOVE_DESC)
        );
        return true;
    }

    private final class InLoveBar extends EntityPropertyProgressBar {
        public InLoveBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer inLoveOpt = inLoveProperty.getVal();
            if (inLoveOpt == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }
            int inLoveTime = inLoveOpt;
            int inLoveMaxTime = IN_LOVE_MAX_TIME;
            updatePercent((float) inLoveTime / inLoveMaxTime);
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, TextUtils.literal(inLoveTime + "t/" + inLoveMaxTime + "t"));
            } else if (isBabyClient()) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_BABY));
            } else {
                renderInnerText(ctx, TextUtils.literal((inLoveTime / 20) + "s/" + (inLoveMaxTime / 20) + "s"));
            }
        }
    }
}

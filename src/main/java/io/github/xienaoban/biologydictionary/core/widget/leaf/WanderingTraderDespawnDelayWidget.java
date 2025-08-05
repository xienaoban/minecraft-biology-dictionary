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
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;

public class WanderingTraderDespawnDelayWidget extends EntityPropertyStandardWidget<WanderingTrader> {
    private static final int L = 1, T = 6;

    /**
     * @see WanderingTraderSpawner#spawn(ServerLevel)
     */
    private static final int MAX_DESPAWN_DELAY = 48000;

    private final IntProperty<WanderingTrader> despawnDelayProperty = VanillaEntityProperties.OfWanderingTrader.getDespawnDelayProperty(p());

    public WanderingTraderDespawnDelayWidget(EntityProperties<WanderingTrader> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new DespawnDelayBar());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer delayI = despawnDelayProperty.get();
        if (delayI == null) {
            return;
        }
        int delay = delayI;
        if (delay > 0) {
            despawnDelayProperty.set(delay - 1);
        }
    }

    private final class DespawnDelayBar extends EntityPropertyProgressBar {
        public DespawnDelayBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer delayI = despawnDelayProperty.get();
            if (delayI == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            int delay = delayI;
            updatePercent((float) delay / MAX_DESPAWN_DELAY);
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(delay + "t/" + MAX_DESPAWN_DELAY + "t"));
            } else if (delay == 0) {
                renderInnerText(ctx, Component.literal("∞/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            } else if (delay < 3 * 60 * 20) {
                renderInnerText(ctx, Component.literal((delay / 20) + "s/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            } else {
                renderInnerText(ctx, Component.literal((delay / 20 / 60) + "min/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            }
        }
    }
}

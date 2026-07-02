package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.WanderingTraderRetainSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public class WanderingTraderDespawnDelayWidget extends EntityPropertyStandardWidget<WanderingTrader> {
    public static final Factory<WanderingTrader> FACTORY = WanderingTraderDespawnDelayWidget::new;

    private static final int L = 6, T = 6;

    /**
     * @see net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner#spawn(ServerLevel)
     */
    private static final int MAX_DESPAWN_DELAY = 48000;

    private final IntProperty<WanderingTrader> despawnDelayProperty = VanillaEntityProperties.OfWanderingTrader.getDespawnDelayProperty(p());

    public WanderingTraderDespawnDelayWidget(EntityProperties<WanderingTrader> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new DespawnDelayBar());
        addElementButton(new RetainButton());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer delayI = despawnDelayProperty.getVal();
        if (delayI == null) {
            return;
        }
        int delay = delayI;
        if (delay > 0) {
            despawnDelayProperty.setVal(delay - 1);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_DESPAWN_DELAY),
                tooltipDescription(Lang.PROPERTY_WIDGET_DESPAWN_DELAY_DESC)
        );
        return true;
    }

    private final class DespawnDelayBar extends EntityPropertyProgressBar {
        public DespawnDelayBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer delayI = despawnDelayProperty.getVal();
            if (delayI == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            int delay = delayI;
            updatePercent((float) delay / MAX_DESPAWN_DELAY);
            super.onRender(ctx);
            if (BiologyDictionaryClient.isDebugMode()) {
                renderInnerText(ctx, TextUtils.literal(delay + "t/" + MAX_DESPAWN_DELAY + "t"));
            } else if (delay == 0) {
                renderInnerText(ctx, TextUtils.literal("∞/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            } else if (delay < 3 * 60 * 20) {
                renderInnerText(ctx, TextUtils.literal((delay / 20) + "s/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            } else {
                renderInnerText(ctx, TextUtils.literal((delay / 20 / 60) + "min/" + (MAX_DESPAWN_DELAY / 20 / 60) + "min"));
            }
        }
    }

    private final class RetainButton extends EntityPropertyButton {
        private final ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);

        public RetainButton() {
            super(null, -1, -1);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                if (BiologySkills.activate(e(), new WanderingTraderRetainSkill())) {
                    despawnDelayProperty.setVal(despawnDelayProperty.getVal() + WanderingTraderRetainSkill.STAY_TICKS);
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            // super.onRender(ctx);
            renderItem(ctx, waterBucket, 1);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = new WanderingTraderRetainSkill().getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_DESPAWN_DELAY_RETAIN));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_DESPAWN_DELAY_RETAIN_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.skill.entity.BeeClearHiveSkill;
import io.github.xienaoban.biologydictionary.core.skill.entity.VillagerForceRestockSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VillagerRestocksTodayWidget extends EntityPropertyStandardWidget<Villager> {
    private static final int L = 6, T = 5;

    private static final int MAX_RESTOCK_TODAY = 2;

    private final IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.getRestocksTodayProperty(p());

    public VillagerRestocksTodayWidget(EntityProperties<Villager> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new RestocksTodayBar());
        addElementButton(new RestockNowButton());
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

    private final class RestockNowButton extends EntityPropertyButton {
        private final ItemStack emerald = new ItemStack(Items.EMERALD);

        public RestockNowButton() {
            super(null, -1, -1);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                VillagerJobSiteProperty jboSiteProperty = p().getExtra(VillagerJobSiteProperty.class);
                Integer r = restocksTodayProperty.get();
                GlobalPos j = jboSiteProperty.get();
                if (VillagerForceRestockSkill.activate(e(), r, j)) {
                    restocksTodayProperty.set(r + 1);
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            ctx.renderItem(emerald, 0.75F, box.getLeft() - 2, box.getTop() - 2);
            Integer numI = restocksTodayProperty.get();
            if (numI != null) {
                int num = numI;
                int price = Math.max(0, num - 3 + 1) * 2;
                ctx.renderCenteredText(Component.literal(String.valueOf(price)), Colors.GRAY, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2F + 0.5F,  box.getTop() + 2F+ 0.5F);
                ctx.renderCenteredText(Component.literal(String.valueOf(price)), Colors.WHITE, 0.5F, ctx.getZ(), (box.getLeft() + box.getRight()) / 2F,  box.getTop() + 2F);
            }
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_BEE_HIVE_CLEAR),
                    tooltipDescription(Lang.PROPERTY_WIDGET_BEE_HIVE_CLEAR_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_EXPERIENCE_POINTS_COST, BeeClearHiveSkill.EXPERIENCE_POINTS_COST)
            );
            return true;
        }
    }
}

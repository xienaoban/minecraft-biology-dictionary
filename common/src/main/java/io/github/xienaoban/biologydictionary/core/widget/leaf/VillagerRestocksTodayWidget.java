package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.VillagerJobSiteProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.VillagerForceRestockSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public class VillagerRestocksTodayWidget extends EntityPropertyStandardWidget<Villager> {
    public static final Factory<Villager> FACTORY = VillagerRestocksTodayWidget::new;

    private static final int L = 6, T = 5;

    private static final int MAX_RESTOCK_TODAY = 2;

    private final IntProperty<Villager> restocksTodayProperty = VanillaEntityProperties.OfVillager.getRestocksTodayProperty(p());
    private final VillagerJobSiteProperty jboSiteProperty = p().getExtra(VillagerJobSiteProperty.class);

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
            Integer numI = restocksTodayProperty.getVal();
            if (numI == null) {
                updatePercent(0);
                super.onRender(ctx);
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                return;
            }

            int num = numI;
            updatePercent((float) num / MAX_RESTOCK_TODAY);
            super.onRender(ctx);
            renderInnerText(ctx, TextUtils.literal(String.valueOf(num)));
        }
    }

    private final class RestockNowButton extends EntityPropertyButton {
        private final ItemStack emerald = new ItemStack(Items.EMERALD);

        public RestockNowButton() {
            super(null, -1, -1);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                Integer r = restocksTodayProperty.getVal();
                if (r != null) {
                    GlobalPos j = jboSiteProperty.getVal();
                    if (BiologySkills.activate(e(), new VillagerForceRestockSkill(r, j))) {
                        restocksTodayProperty.setVal(r + 1);
                    }
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            // super.onRender(ctx);
            Integer numI = restocksTodayProperty.getVal();
            Integer price = (numI == null ? null : Math.max(0, numI - 3 + 1) * 2);
            renderItem(ctx, emerald, price);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            Integer r = restocksTodayProperty.getVal();
            GlobalPos j = jboSiteProperty.getVal();
            SkillCost cost = new VillagerForceRestockSkill(r == null ? 0 : r, j).getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_RESTOCKS_TODAY_RESTOCK));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_RESTOCKS_TODAY_RESTOCK_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

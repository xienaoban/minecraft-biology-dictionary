package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.EntityInventorySizeProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.LivingEntityStealInventorySkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyTextBar;
import io.github.xienaoban.biologydictionary.gui.screen.misc.InventoryStealingScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class LivingEntityInventoryWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityInventoryWidget::new;

    private static final int L = 11, T = 6;
    private static final int L_STEAL = 21, T_STEAL = 5;

    private final EntityInventorySizeProperty inventorySize = p().getExtra(EntityInventorySizeProperty.class);

    public LivingEntityInventoryWidget(EntityProperties<LivingEntity> properties) {
        super(properties);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        setElementBar(new InventorySizeBar());
        addElementButton(new StealButton());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_INVENTORY),
                tooltipDescription(Lang.PROPERTY_WIDGET_INVENTORY_DESC1),
                tooltipDescription(Lang.PROPERTY_WIDGET_INVENTORY_DESC2)
        );
        return true;
    }

    private final class InventorySizeBar extends EntityPropertyTextBar {
        public InventorySizeBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            Integer size = inventorySize.getVal();
            if (size == null) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
            } else {
                renderInnerText(ctx, TextUtils.literal("" + inventorySize.getVal()));
            }
        }
    }

    private final class StealButton extends EntityPropertyButton {

        public StealButton() {
            super(Textures.ICONS, L_STEAL * WIDGET_WIDTH, T_STEAL * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                // Check if entity is looking at the player before opening the screen
                if (InventoryStealingScreen.isPlayerCaughtByEntity(e(), ClientUtils.getClientPlayer())) {
                    BiologyDictionaryClient.sendCenteredWarning(TextUtils.translate(Lang.TEXT_ENTITY_LOOKING_AT_YOU));
                    return true;
                }
                BiologySkills.activate(e(), new LivingEntityStealInventorySkill());
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = new LivingEntityStealInventorySkill().getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_INVENTORY_STEAL));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_INVENTORY_STEAL_DESC1));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_INVENTORY_STEAL_DESC2));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

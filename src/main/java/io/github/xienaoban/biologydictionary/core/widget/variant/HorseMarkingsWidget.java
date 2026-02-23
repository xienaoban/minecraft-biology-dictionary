package io.github.xienaoban.biologydictionary.core.widget.variant;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetVariantSkill;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class HorseMarkingsWidget extends AbstractEntityStandardVariantWidget<Horse, Markings> {
    public static final Factory<Horse> FACTORY = HorseMarkingsWidget::new;

    private static final int VH_IDX = 1;

    public HorseMarkingsWidget(EntityProperties<Horse> properties) {
        super(properties, getVariantCount(properties, VH_IDX));
        setBackgroundBars(Textures.ICONS, BG_BAR2_LEFT * Widget.WIDGET_WIDTH, BG_BAR2_TOP * Widget.WIDGET_HEIGHT);
    }

    @Override
    protected int getVariantHandlerIdx() { return VH_IDX; }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        // Use placeholder values since variant is selected at runtime
        SkillCost cost = new EntitySetVariantSkill("", -1, null).getRealCost(e());
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_MARKINGS));
        tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_MARKINGS_DESC));
        tooltip.add(TextUtils.empty());
        tooltip.addAll(cost.toTooltipText());
        renderTooltip(ctx, tooltip);
        return true;
    }
}

package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.SheepForceEatGrassSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Sheep;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public class SheepEatGrassWidget extends EntityPropertyStandardWidget<Sheep> {
    public static final Factory<Sheep> FACTORY = SheepEatGrassWidget::new;

    private static final int L = 19, T = 2;
    private static final int L_GRASS = 21, T_GRASS = 4;

    public SheepEatGrassWidget(EntityProperties<Sheep> properties) {
        super(properties, Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new EatGrassButton());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_EAT_GRASS),
                tooltipDescription(Lang.PROPERTY_WIDGET_EAT_GRASS_DESC)
        );
        return true;
    }

    private final class EatGrassButton extends EntityPropertyButton {

        public EatGrassButton() {
            super(Textures.ICONS, L_GRASS * WIDGET_WIDTH, T_GRASS * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            if (isMouseLeft(button)) {
                if (!SheepForceEatGrassSkill.isGrassOrGrassBlock(e())) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(TextUtils.translate(Lang.TEXT_SHEEP_NO_GRASS_UNDER_FEET));
                } else if (BiologySkills.activate(e(), new SheepForceEatGrassSkill())) {
                    ClientUtils.getCurrentScreen().onClose();
                }
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = new SheepForceEatGrassSkill().getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_EAT_GRASS));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_EAT_GRASS_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

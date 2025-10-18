package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.entity.SheepForceEatGrassSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.sheep.Sheep;

public class SheepEatGrassWidget extends EntityPropertyStandardWidget<Sheep> {
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
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                if (!SheepForceEatGrassSkill.isGrassOrGrassBlock(e())) {
                    AbstractBiologyDictionaryScreen.current()
                            .sendScreenMessage(Component.translatable(Lang.TEXT_SHEEP_NO_GRASS_UNDER_FEET));
                } else if (SheepForceEatGrassSkill.activate(e())) {
                    ClientUtils.getCurrentScreen().onClose();
                }
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_EAT_GRASS),
                    tooltipDescription(Lang.PROPERTY_WIDGET_EAT_GRASS_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_EXPERIENCE_POINTS_COST, SheepForceEatGrassSkill.EXP_COST)
            );
            return true;
        }
    }
}

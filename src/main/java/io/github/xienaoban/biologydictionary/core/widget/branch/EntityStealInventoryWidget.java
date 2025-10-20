package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetSoundSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.Entity;

public abstract class EntityStealInventoryWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 20, T = 1;

    private static EntityProperties<Entity> verify(EntityProperties<Entity> properties) {
        Entity entity = properties.entity();
        return properties;
    }

    public EntityStealInventoryWidget(EntityProperties<Entity> properties) {
        super(verify(properties), Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new OpenStealScreenButton());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_VARIANT),
                tooltipDescription(Lang.PROPERTY_WIDGET_VARIANT_DESC)
        );
        return true;
    }

    private final class OpenStealScreenButton extends EntityPropertyButton {
        public OpenStealScreenButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                // if (EntitySetSoundSkill.activate(e(), getInventory())) {
                // }
            }
            return true;
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_SOUND_SWITCH),
                    tooltipDescription(Lang.PROPERTY_WIDGET_SOUND_SWITCH_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_EXPERIENCE_POINTS_COST, EntitySetSoundSkill.experiencePointsCost(e()))
            );
            return true;
        }
    }
}

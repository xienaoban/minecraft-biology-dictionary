package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetInvulnerableSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class EntityInvulnerableWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 19, T = 1;

    private final BooleanProperty<Entity> invulnerableProperty = VanillaEntityProperties.OfEntity.getInvulnerableProperty(p());

    public EntityInvulnerableWidget(EntityProperties<Entity> properties) {
        super(properties, Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new InvulnerableButton());
    }

    private boolean isInvulnerable() {
        Boolean inv = invulnerableProperty.getVal();
        return inv != null && inv;
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_INVULNERABLE),
                tooltipDescription(Lang.PROPERTY_WIDGET_INVULNERABLE_DESC)
        );
        return true;
    }

    private final class InvulnerableButton extends EntityPropertyButton {

        public InvulnerableButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean newInv = !isInvulnerable();
                if (EntitySetInvulnerableSkill.activate(e(), newInv)) {
                    invulnerableProperty.setVal(newInv);
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isInvulnerable() ? 0 : 1) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_INVULNERABLE_SWITCH),
                    tooltipDescription(Lang.PROPERTY_WIDGET_INVULNERABLE_SWITCH_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_ONLY_IN_CREATIVE_MODE)
            );
            return true;
        }
    }
}

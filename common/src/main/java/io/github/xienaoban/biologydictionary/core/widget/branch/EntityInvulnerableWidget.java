package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.EntitySetInvulnerableSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class EntityInvulnerableWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityInvulnerableWidget::new;

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
                if (BiologySkills.activate(e(), new EntitySetInvulnerableSkill(newInv))) {
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
            SkillCost cost = new EntitySetInvulnerableSkill(!isInvulnerable()).getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_INVULNERABLE_SWITCH));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_INVULNERABLE_SWITCH_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

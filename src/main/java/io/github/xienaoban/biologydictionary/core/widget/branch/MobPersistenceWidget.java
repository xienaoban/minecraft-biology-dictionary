package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.core.property.extra.MobNaturalPersistenceProperty;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.entity.MobForcePersistentSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MobPersistenceWidget extends EntityPropertyStandardWidget<Mob> {
    public static final Factory<Mob> FACTORY = MobPersistenceWidget::new;

    private static final int L = 6, T = 6;

    private final BooleanProperty<Mob> persistenceRequiredProperty = VanillaEntityProperties.OfMob.getPersistenceRequiredProperty(p());
    private final MobNaturalPersistenceProperty naturalPersistenceProperty = p().getExtra(MobNaturalPersistenceProperty.class);

    public MobPersistenceWidget(EntityProperties<Mob> properties) {
        super(properties, Page.COLUMNS / 2);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new ForcedPersistentButton());
        addElementButton(new PersistentButton());
    }

    private boolean isForcedPersistent() {
        Boolean persistent = persistenceRequiredProperty.getVal();
        return persistent != null && persistent;
    }

    private boolean isNaturalPersistent() {
        Boolean persistent = naturalPersistenceProperty.getVal();
        return persistent != null && persistent;
    }

    /**
     * @see net.minecraft.world.entity.Mob#checkDespawn()
     */
    private boolean isPersistent() {
        return isForcedPersistent() || isNaturalPersistent();
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_PERSISTENCE),
                tooltipDescription(Lang.PROPERTY_WIDGET_PERSISTENCE_DESC)
        );
        return true;
    }

    private final class ForcedPersistentButton extends EntityPropertyButton {

        public ForcedPersistentButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean persistent = !isForcedPersistent();
                if (BiologySkills.activate(e(), new MobForcePersistentSkill(persistent))) {
                    persistenceRequiredProperty.setVal(persistent);
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isForcedPersistent() ? 0 : 1) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = MobForcePersistentSkill.META.getDefaultCost();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_PERSISTENCE_FORCED));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_PERSISTENCE_FORCED_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }

    private final class PersistentButton extends EntityPropertyButton {

        public PersistentButton() {
            super(Textures.ICONS, L_YES_NO * WIDGET_WIDTH, T_YES_NO * WIDGET_HEIGHT);
            setSelectable(false);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isPersistent() ? 0 : 1) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_PERSISTENCE_FINAL),
                    tooltipDescription(Lang.PROPERTY_WIDGET_PERSISTENCE_FINAL_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.PROPERTY_WIDGET_PERSISTENCE_FINAL_FORCED, Lang.textYesOrNo(isForcedPersistent())),
                    tooltipBody(Lang.PROPERTY_WIDGET_PERSISTENCE_FINAL_NATURAL, Lang.textYesOrNo(isNaturalPersistent()))
            );
            return true;
        }
    }
}

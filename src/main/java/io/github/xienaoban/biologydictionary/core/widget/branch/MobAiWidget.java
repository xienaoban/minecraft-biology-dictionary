package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.entity.MobSetNoAiSkill;
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
public final class MobAiWidget extends EntityPropertyStandardWidget<Mob> {
    public static final Factory<Mob> FACTORY = MobAiWidget::new;

    private static final int L = 18, T = 1;

    public MobAiWidget(EntityProperties<Mob> properties) {
        super(properties, Page.COLUMNS / 4);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, T * WIDGET_HEIGHT));
        addElementButton(new AiButton());
    }

    private boolean isNoAi() {
        return e().isNoAi();
    }

    private void setNoAi(boolean ai) {
        e().setNoAi(ai);
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_AI),
                tooltipDescription(Lang.PROPERTY_WIDGET_AI_DESC)
        );
        return true;
    }

    private final class AiButton extends EntityPropertyButton {

        public AiButton() {
            super(Textures.ICONS, L_ON_OFF * WIDGET_WIDTH, T_ON_OFF * WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            if (isMouseLeft(code)) {
                boolean newNoAi = !isNoAi();
                if (BiologySkills.activate(e(), new MobSetNoAiSkill(newNoAi))) {
                    setNoAi(newNoAi);
                    if (!PlayerUtils.isCreative(ClientUtils.getClientPlayer())) {
                        if (newNoAi) {
                            VanillaEntityProperties.OfMob.getPersistenceRequiredProperty(p()).setVal(true);
                        }
                        VanillaEntityProperties.OfEntity.getInvulnerableProperty(p()).setVal(newNoAi);
                    }
                }
            }
            return true;
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            setTextureLeftOffset((isNoAi() ? 1 : 0) * WIDGET_WIDTH);
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = MobSetNoAiSkill.META.getDefaultCost();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_AI_SWITCH));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_AI_SWITCH_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

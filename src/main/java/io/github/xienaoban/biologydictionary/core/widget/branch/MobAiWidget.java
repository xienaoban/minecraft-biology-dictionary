package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.McClientUtils;
import io.github.xienaoban.biologydictionary.common.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.core.skill.impl.MobSetNoAiSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

@Environment(EnvType.CLIENT)
public final class MobAiWidget extends EntityPropertyStandardWidget<Mob> {
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
                if (MobSetNoAiSkill.activate(e(), newNoAi)) {
                    setNoAi(newNoAi);
                    if (!PlayerUtils.isCreative(McClientUtils.getClientPlayer())) {
                        BooleanProperty<Entity> inv = VanillaEntityProperties.OfEntity.getInvulnerableProperty(p());
                        inv.set(newNoAi);
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
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_AI_SWITCH),
                    tooltipDescription(Lang.PROPERTY_WIDGET_AI_SWITCH_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_EXPERIENCE_LEVELS_REQUIRED, MobSetNoAiSkill.experienceLevelsRequired(e())),
                    tooltipBody(Lang.TEXT_EXPERIENCE_LEVELS_COST, MobSetNoAiSkill.experienceLevelsCost(e()))
            );
            return true;
        }
    }
}

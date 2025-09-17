package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.skill.Skills;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

@Environment(EnvType.CLIENT)
public final class AgeableMobGrowthWidget extends EntityPropertyStandardWidget<AgeableMob> {
    private static final int L = 1, T = 4;

    private static final int BABY_MIN_AGE = AgeableMob.BABY_START_AGE;
    private static final int ADULT_MIN_AGE = 0;

    private final IntProperty<AgeableMob> ageProperty = VanillaEntityProperties.OfAgeableMob.getAgeProperty(p());
    private final IntProperty<AgeableMob> forcedAgeProperty = VanillaEntityProperties.OfAgeableMob.getForcedAgeProperty(p());

    public AgeableMobGrowthWidget(EntityProperties<AgeableMob> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new GrowthBar());
        addElementButton(new LockInBabyButton());
    }

    private boolean isAdultClient() {
        return !EntityUtils.isBaby(e());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer ageOpt = ageProperty.get();
        if (ageOpt == null) {
            return;
        }
        int age = ageOpt;
        if (age < ADULT_MIN_AGE) {
            ageProperty.set(age + 1);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_GROWTH),
                tooltipDescription(Lang.PROPERTY_WIDGET_GROWTH_DESC)
                );
        return true;
    }

    private final class GrowthBar extends EntityPropertyProgressBar {
        public GrowthBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer ageOpt = ageProperty.get();
            Integer forcedAgeOpt = forcedAgeProperty.get();
            if (ageOpt == null || forcedAgeOpt == null) {
                if (isAdultClient()) {
                    updatePercent(1F);
                } else {
                    updatePercent(0);
                }
                super.onRender(ctx);
                if (ctx.isDebug()) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                } else {
                    if (isAdultClient()) {
                        renderInnerText(ctx, Component.translatable(Lang.TEXT_ADULT));
                    } else {
                        renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                    }
                }
                return;
            }
            int age = ageOpt;
            int forcedAge = forcedAgeOpt;
            updatePercent(forcedAge < ADULT_MIN_AGE ? 0F : (1F - (float) age / BABY_MIN_AGE));
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(age + "t/" + ADULT_MIN_AGE + "t"));
            } else if (!isAdultClient()) {
                if (forcedAge < ADULT_MIN_AGE) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_ALWAYS_BABY));
                } else {
                    renderInnerText(ctx, Component.literal(((age - BABY_MIN_AGE) / 20) + "s/" + (-BABY_MIN_AGE / 20 / 60) + "m"));
                }
            } else {
                renderInnerText(ctx, Component.translatable(Lang.TEXT_ADULT));
            }
        }
    }

    private final class LockInBabyButton extends EntityPropertyButton {
        public LockInBabyButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            Integer forcedAgeOpt = forcedAgeProperty.get();
            if (forcedAgeOpt == null) {
                return true;
            }
            if (isAdultClient()) {
                // Do nothing if it is an adult.
                return true;
            }

            int forcedAge = forcedAgeOpt;
            if (isMouseLeft(code)) {
                final int newForcedAge;
                if (forcedAge == ADULT_MIN_AGE) {
                    newForcedAge = BABY_MIN_AGE;
                } else {
                    newForcedAge = ADULT_MIN_AGE;
                }

                if (ClientNetManager.sendEntityOrientedSkill(e(), Skills.AGEABLE_MOB_SET_FORCED_AGE, newForcedAge, BABY_MIN_AGE)) {
                    forcedAgeProperty.set(newForcedAge);
                    ageProperty.set(BABY_MIN_AGE);
                }
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (isAdultClient()) {
                // Treat adult as locked as it will never change state.
                setTextureLeftOffset(10);
            } else {
                Integer forcedAge = forcedAgeProperty.get();
                if (forcedAge != null && forcedAge < ADULT_MIN_AGE) {
                    setTextureLeftOffset(10);
                } else {
                    setTextureLeftOffset(0);
                }
            }
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            renderTooltip(ctx,
                    tooltipTitle(Lang.PROPERTY_WIDGET_GROWTH_LOCK),
                    tooltipDescription(Lang.PROPERTY_WIDGET_GROWTH_LOCK_DESC)
            );
            return true;
        }
    }
}

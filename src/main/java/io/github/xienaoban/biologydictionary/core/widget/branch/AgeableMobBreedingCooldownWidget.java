package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.entity.AgeableMobSetForcedAgeSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.mixin.AnimalIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

@Environment(EnvType.CLIENT)
public final class AgeableMobBreedingCooldownWidget extends EntityPropertyStandardWidget<AgeableMob> {
    public static final Factory<AgeableMob> FACTORY = AgeableMobBreedingCooldownWidget::new;

    private static final int L = 6, T = 4;

    /**
     * @see net.minecraft.world.entity.animal.Animal#PARENT_AGE_AFTER_BREEDING
     */
    private static final int BREED_COOLDOWN_MAX = AnimalIMixin.getParentAgeAfterBreeding();
    private static final int BREED_COOLDOWN_OFF = 0;

    private final IntProperty<AgeableMob> ageProperty = VanillaEntityProperties.OfAgeableMob.getAgeProperty(p());
    private final IntProperty<AgeableMob> forcedAgeProperty = VanillaEntityProperties.OfAgeableMob.getForcedAgeProperty(p());

    public AgeableMobBreedingCooldownWidget(EntityProperties<AgeableMob> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new BreedingBar());
        addElementButton(new LockNeverBreedButton());
    }

    private boolean isAdultClient() {
        return !EntityUtils.isBaby(e());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        Integer ageOpt = ageProperty.getVal();
        if (ageOpt == null) {
            return;
        }
        int age = ageOpt;
        if (age > BREED_COOLDOWN_OFF) {
            ageProperty.setVal(age - 1);
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN),
                tooltipDescription(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN_DESC)
        );
        return true;
    }

    private final class BreedingBar extends EntityPropertyProgressBar {
        public BreedingBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer ageOpt = ageProperty.getVal();
            Integer forcedAgeOpt = forcedAgeProperty.getVal();
            if (ageOpt == null || forcedAgeOpt == null) {
                updatePercent(0);
                super.onRender(ctx);
                if (ctx.isDebug()) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                } else {
                    if (isAdultClient()) {
                        renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                    } else {
                        renderInnerText(ctx, Component.translatable(Lang.TEXT_BABY));
                    }
                }
                return;
            }
            int age = ageOpt;
            int forcedAge = forcedAgeOpt;
            updatePercent(forcedAge > BREED_COOLDOWN_OFF ? 1 : ((float) age / BREED_COOLDOWN_MAX));
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(age + "t/" + BREED_COOLDOWN_MAX + "t"));
            } else if (isAdultClient()) {
                if (forcedAge > BREED_COOLDOWN_OFF) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_NEVER_BREED));
                } else {
                    renderInnerText(ctx, Component.literal((age / 20) + "s/" + (BREED_COOLDOWN_MAX / 20 / 60) + "m"));
                }
            } else {
                renderInnerText(ctx, Component.translatable(Lang.TEXT_BABY));
            }
        }
    }

    private final class LockNeverBreedButton extends EntityPropertyButton {
        public LockNeverBreedButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            Integer forcedAgeOpt = forcedAgeProperty.getVal();
            if (forcedAgeOpt == null) {
                return true;
            }
            if (!isAdultClient()) {
                // Do nothing if it is a baby.
                return true;
            }

            int forcedAge = forcedAgeOpt;
            if (isMouseLeft(code)) {
                final int newForcedAge;
                if (forcedAge == BREED_COOLDOWN_OFF) {
                    newForcedAge = BREED_COOLDOWN_MAX;
                } else {
                    newForcedAge = BREED_COOLDOWN_OFF;
                }

                if (AgeableMobSetForcedAgeSkill.activate(e(), newForcedAge, BREED_COOLDOWN_MAX)) {
                    forcedAgeProperty.setVal(newForcedAge);
                    ageProperty.setVal(BREED_COOLDOWN_MAX);
                }
            }
            return super.onMouseDown(x, y, code);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (!isAdultClient()) {
                // Treat adult as locked as it will never change state.
                setTextureLeftOffset(10);
            } else {
                Integer forcedAge = forcedAgeProperty.getVal();
                if (forcedAge != null && forcedAge > BREED_COOLDOWN_OFF) {
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
                    tooltipTitle(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN_LOCK),
                    tooltipDescription(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN_LOCK_DESC),
                    tooltipEmpty(),
                    tooltipBody(Lang.TEXT_EXPERIENCE_POINTS_COST, AgeableMobSetForcedAgeSkill.EXPERIENCE_POINTS_COST)
            );
            return true;
        }
    }
}

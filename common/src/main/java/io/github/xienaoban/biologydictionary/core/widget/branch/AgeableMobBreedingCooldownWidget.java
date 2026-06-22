package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.AgeableMobSetBreedingCooldownSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.mixin.entity.AnimalIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public final class AgeableMobBreedingCooldownWidget extends EntityPropertyStandardWidget<AgeableMob> {
    public static final Factory<AgeableMob> FACTORY = AgeableMobBreedingCooldownWidget::new;

    private static final int L = 6, T = 4;

    /**
     * @see net.minecraft.world.entity.animal.Animal#PARENT_AGE_AFTER_BREEDING
     */
    private static final int BREED_COOLDOWN_MAX = AnimalIMixin.biologydictionary$getParentAgeAfterBreeding();
    private static final int BREED_COOLDOWN_OFF = 0;
    private static final int BREED_COOLDOWN_LOCKED = BREED_COOLDOWN_MAX + 1;

    private final IntProperty<AgeableMob> ageProperty = VanillaEntityProperties.OfAgeableMob.getAgeProperty(p());

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
        if (age > BREED_COOLDOWN_OFF && !isBreedLocked()) {
            ageProperty.setVal(age - 1);
        }
    }

    private boolean isBreedLocked() {
        Integer age = ageProperty.getVal();
        return age != null && age == BREED_COOLDOWN_LOCKED;
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
            if (ageOpt == null) {
                updatePercent(0);
                super.onRender(ctx);
                if (ctx.isDebug()) {
                    renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                } else {
                    if (isAdultClient()) {
                        renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                    } else {
                        renderInnerText(ctx, TextUtils.translate(Lang.TEXT_BABY));
                    }
                }
                return;
            }
            int age = ageOpt;
            updatePercent(isBreedLocked() ? 1 : ((float) age / BREED_COOLDOWN_MAX));
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, TextUtils.literal(age + "t/" + BREED_COOLDOWN_MAX + "t"));
            } else if (isAdultClient()) {
                if (isBreedLocked()) {
                    renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NEVER_BREED));
                } else {
                    renderInnerText(ctx, TextUtils.literal((age / 20) + "s/" + (BREED_COOLDOWN_MAX / 20 / 60) + "m"));
                }
            } else {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_BABY));
            }
        }
    }

    private final class LockNeverBreedButton extends EntityPropertyButton {
        public LockNeverBreedButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            Integer ageOpt = ageProperty.getVal();
            if (ageOpt == null) {
                return true;
            }
            if (!isAdultClient()) {
                // Do nothing if it is a baby.
                return true;
            }

            if (isMouseLeft(code)) {
                final int newAge;
                if (ageOpt == BREED_COOLDOWN_LOCKED) {
                    newAge = BREED_COOLDOWN_MAX;
                } else {
                    newAge = BREED_COOLDOWN_LOCKED;
                }

                if (BiologySkills.activate(e(), new AgeableMobSetBreedingCooldownSkill(newAge))) {
                    ageProperty.setVal(newAge);
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
                if (isBreedLocked()) {
                    setTextureLeftOffset(10);
                } else {
                    setTextureLeftOffset(0);
                }
            }
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            int targetAge = isBreedLocked() ? BREED_COOLDOWN_MAX : BREED_COOLDOWN_LOCKED;
            SkillCost cost = new AgeableMobSetBreedingCooldownSkill(targetAge).getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN_LOCK));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_BREEDING_COOLDOWN_LOCK_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

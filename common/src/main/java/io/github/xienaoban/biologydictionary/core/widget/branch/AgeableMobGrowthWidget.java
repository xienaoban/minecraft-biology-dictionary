package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.BooleanProperty;
import io.github.xienaoban.biologydictionary.core.property.builtin.IntProperty;
import io.github.xienaoban.biologydictionary.core.skill.BiologySkills;
import io.github.xienaoban.biologydictionary.core.skill.SkillCost;
import io.github.xienaoban.biologydictionary.core.skill.entity.AgeableMobSetAgeLockedSkill;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public final class AgeableMobGrowthWidget extends EntityPropertyStandardWidget<AgeableMob> {
    public static final Factory<AgeableMob> FACTORY = AgeableMobGrowthWidget::new;

    private static final int L = 1, T = 4;

    private static final int BABY_MIN_AGE = AgeableMob.BABY_START_AGE;
    private static final int ADULT_MIN_AGE = 0;

    private final IntProperty<AgeableMob> ageProperty = VanillaEntityProperties.OfAgeableMob.getAgeProperty(p());
    private final BooleanProperty<AgeableMob> ageLockedProperty =
            VanillaEntityProperties.OfAgeableMob.getAgeLockedProperty(p());

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
        Integer ageOpt = ageProperty.getVal();
        if (ageOpt == null) {
            return;
        }
        if (isAgeLocked()) {
            return;
        }
        int age = ageOpt;
        if (age < ADULT_MIN_AGE) {
            ageProperty.setVal(age + 1);
        }
    }

    private boolean isAgeLocked() {
        Boolean ageLocked = ageLockedProperty.getVal();
        return ageLocked != null && ageLocked;
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
            Integer ageOpt = ageProperty.getVal();
            Boolean ageLockedOpt = ageLockedProperty.getVal();
            if (ageOpt == null || ageLockedOpt == null) {
                if (isAdultClient()) {
                    updatePercent(1F);
                } else {
                    updatePercent(0);
                }
                super.onRender(ctx);
                if (BiologyDictionaryClient.isDebugMode()) {
                    renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                } else {
                    if (isAdultClient()) {
                        renderInnerText(ctx, TextUtils.translate(Lang.TEXT_ADULT));
                    } else {
                        renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS));
                    }
                }
                return;
            }
            int age = ageOpt;
            boolean ageLocked = ageLockedOpt;
            updatePercent(ageLocked ? 0F : (1F - (float) age / BABY_MIN_AGE));
            super.onRender(ctx);
            if (BiologyDictionaryClient.isDebugMode()) {
                renderInnerText(ctx, TextUtils.literal(age + "t/" + ADULT_MIN_AGE + "t"));
            } else if (!isAdultClient()) {
                if (ageLocked) {
                    renderInnerText(ctx, TextUtils.translate(Lang.TEXT_ALWAYS_BABY));
                } else {
                    renderInnerText(ctx, TextUtils.literal(
                            ((age - BABY_MIN_AGE) / 20) + "s/" + (-BABY_MIN_AGE / 20 / 60) + "m"));
                }
            } else {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_ADULT));
            }
        }
    }

    private final class LockInBabyButton extends EntityPropertyButton {
        public LockInBabyButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected boolean onMouseDown(float mouseX, float mouseY, int button) {
            Boolean ageLockedOpt = ageLockedProperty.getVal();
            if (ageLockedOpt == null) {
                return true;
            }
            if (isAdultClient()) {
                // Do nothing if it is an adult.
                return true;
            }

            if (isMouseLeft(button)) {
                boolean newAgeLocked = !ageLockedOpt;
                if (BiologySkills.activate(e(), new AgeableMobSetAgeLockedSkill(newAgeLocked))) {
                    ageLockedProperty.setVal(newAgeLocked);
                    ageProperty.setVal(BABY_MIN_AGE);
                }
            }
            return super.onMouseDown(mouseX, mouseY, button);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (isAdultClient()) {
                // Treat adult as locked as it will never change state.
                setTextureLeftOffset(10);
            } else {
                if (isAgeLocked()) {
                    setTextureLeftOffset(10);
                } else {
                    setTextureLeftOffset(0);
                }
            }
            super.onRender(ctx);
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            SkillCost cost = new AgeableMobSetAgeLockedSkill(!isAgeLocked()).getRealCost(e());
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(tooltipTitle(Lang.PROPERTY_WIDGET_GROWTH_LOCK));
            tooltip.add(tooltipDescription(Lang.PROPERTY_WIDGET_GROWTH_LOCK_DESC));
            tooltip.add(TextUtils.empty());
            tooltip.addAll(cost.toTooltipText());
            renderTooltip(ctx, tooltip);
            return true;
        }
    }
}

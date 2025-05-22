package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.property.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

@Environment(EnvType.CLIENT)
public class AgeableMobAgeWidget extends EntityPropertyStandardWidget<AgeableMob> {
    private static final int L = 1, T = 3;

    private static final int BABY_MIN_AGE = AgeableMob.BABY_START_AGE;
    private static final int ADULT_MIN_AGE = 0;

    private final IntProperty<AgeableMob> ageProperty = EntityVanillaProperties.OfAgeableMob.getAgeProperty(p());
    private final IntProperty<AgeableMob> forcedAgeProperty = EntityVanillaProperties.OfAgeableMob.getForcedAgeProperty(p());

    public AgeableMobAgeWidget(EntityProperties<AgeableMob> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new AgeBar());
        addElementButton(new LockBabyButton());
    }

    private boolean isAdultClient() {
        return e().getAge() >= ADULT_MIN_AGE;
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (isAdultClient() || ageProperty.get() == null) {
            return;
        }
        int age = ageProperty.get();
        if (age < 0) {
            ageProperty.set(Math.min(0, age + 1));
        }
    }

    private final class AgeBar extends EntityPropertyProgressBar {
        public AgeBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            if (ageProperty.get() == null || forcedAgeProperty.get() == null) {
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
                        renderInnerText(ctx, Component.literal("?s/" + (-BABY_MIN_AGE / 20 / 60) + "m"));
                    }
                }
                return;
            }
            int age = ageProperty.get();
            int forcedAge = forcedAgeProperty.get();
            updatePercent(forcedAge < ADULT_MIN_AGE ? 0F : (1F - (float) age / BABY_MIN_AGE));
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(age + "t/" + ADULT_MIN_AGE + "t"));
            } else if (age < ADULT_MIN_AGE) {
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

    private final class LockBabyButton extends EntityPropertyButton {
        public LockBabyButton() {
            super(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
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
        protected boolean onMouseDown(float x, float y, int code) {
            if (forcedAgeProperty.get() == null) {
                return true;
            }
            if (isAdultClient()) {
                // Do nothing if it is adult.
                return true;
            }

            int forcedAge = forcedAgeProperty.get();
            if (isMouseLeft(code)) {
                final int toSet;
                if (forcedAge == ADULT_MIN_AGE) {
                    toSet = BABY_MIN_AGE;
                } else {
                    toSet = ADULT_MIN_AGE;
                }

                // Send to the server.
                IntProperty<AgeableMob> property = EntityVanillaProperties.OfAgeableMob.createForcedAgeProperty();
                property.set(toSet);
                ClientNetManager.sendUpdatedEntityProperties(e(), property.toNbt(), null);
                forcedAgeProperty.set(toSet);
            }
            return super.onMouseDown(x, y, code);
        }
    }
}

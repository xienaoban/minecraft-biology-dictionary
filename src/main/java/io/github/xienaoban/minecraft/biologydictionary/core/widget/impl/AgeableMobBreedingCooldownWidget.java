package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.property.IntProperty;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;

@Environment(EnvType.CLIENT)
public final class AgeableMobBreedingCooldownWidget extends EntityPropertyStandardWidget<AgeableMob> {
    private static final int L = 6, T = 4;

    /**
     * @see net.minecraft.world.entity.animal.Animal#PARENT_AGE_AFTER_BREEDING
     */
    private static final int BREED_MAX = 6000;
    private static final int BREED_OFF = 0;

    private final IntProperty<AgeableMob> ageProperty = EntityVanillaProperties.OfAgeableMob.getAgeProperty(p());
    private final IntProperty<AgeableMob> forcedAgeProperty = EntityVanillaProperties.OfAgeableMob.getForcedAgeProperty(p());

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
        Integer ageOpt = ageProperty.get();
        if (ageOpt == null) {
            return;
        }
        int age = ageOpt;
        if (age > BREED_OFF) {
            ageProperty.set(age - 1);
        }
    }

    private final class BreedingBar extends EntityPropertyProgressBar {
        public BreedingBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Integer ageOpt = ageProperty.get();
            Integer forcedAgeOpt = forcedAgeProperty.get();
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
            updatePercent(forcedAge > BREED_OFF ? 1 : ((float) age / BREED_MAX));
            super.onRender(ctx);
            if (ctx.isDebug()) {
                renderInnerText(ctx, Component.literal(age + "t/" + BREED_MAX + "t"));
            } else if (isAdultClient()) {
                if (forcedAge > BREED_OFF) {
                    renderInnerText(ctx, Component.translatable(Lang.TEXT_NEVER_BREED));
                } else {
                    renderInnerText(ctx, Component.literal((age / 20) + "s/" + (BREED_MAX / 20 / 60) + "m"));
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
        protected void onRender(ScreenRenderingContext ctx) {
            if (!isAdultClient()) {
                // Treat adult as locked as it will never change state.
                setTextureLeftOffset(10);
            } else {
                Integer forcedAge = forcedAgeProperty.get();
                if (forcedAge != null && forcedAge > BREED_OFF) {
                    setTextureLeftOffset(10);
                } else {
                    setTextureLeftOffset(0);
                }
            }
            super.onRender(ctx);
        }

        @Override
        protected boolean onMouseDown(float x, float y, int code) {
            Integer forcedAgeOpt = forcedAgeProperty.get();
            if (forcedAgeOpt == null) {
                return true;
            }
            if (!isAdultClient()) {
                // Do nothing if it is a baby.
                return true;
            }

            int forcedAge = forcedAgeOpt;
            if (isMouseLeft(code)) {
                boolean lock = false;
                final int toSet;
                if (forcedAge == BREED_OFF) {
                    toSet = BREED_MAX;
                    lock = true;
                } else {
                    toSet = BREED_OFF;
                }

                // Send to the server.
                IntProperty<AgeableMob> property = EntityVanillaProperties.OfAgeableMob.createForcedAgeProperty();
                property.set(toSet);
                forcedAgeProperty.set(toSet);
                CompoundTag nbt = property.toNbt();
                if (lock) {
                    IntProperty<AgeableMob> ap = EntityVanillaProperties.OfAgeableMob.createAgeProperty();
                    ap.set(toSet);
                    ageProperty.set(toSet);
                    ap.writeTo(nbt);
                }
                ClientNetManager.sendUpdatedEntityProperties(e(), nbt, null);
            }
            return super.onMouseDown(x, y, code);
        }
    }
}

package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityVanillaProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.vanilla.LivingEntityActiveEffectsProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Colors;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class LivingEntityActiveEffectsWidget extends EntityPropertyStandardWidget<LivingEntity> {
    private static final int L = 11, T = 3;

    private final LivingEntityActiveEffectsProperty activeEffectsProperty
            = EntityVanillaProperties.OfLivingEntity.getActiveEffectsProperty(p());

    public LivingEntityActiveEffectsWidget(EntityProperties<LivingEntity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new ActiveEffectsBar());
    }

    private final class ActiveEffectsBar extends EntityPropertyProgressBar {
        private float gap;
        private int lastSize = 0;

        public ActiveEffectsBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            Component text = null;
            Map<Holder<MobEffect>, MobEffectInstance> effects = activeEffectsProperty.get();
            if (effects == null) {
                text = Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS);
            } else if (effects.isEmpty()) {
                text = Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS);
            }
            updatePercent(text != null ? 0 : 1);
            super.onRender(ctx);
            if (text != null) {
                renderInnerText(ctx, text, Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            if (lastSize != effects.size()) {
                lastSize = effects.size();
                updateGap(lastSize);
            }

            for (int i = effects.size() - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 21 * Widget.WIDGET_WIDTH, 3 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
            }
            int i = -1;
            for (Holder<MobEffect> effect : effects.keySet()) {
                ++i;
                ctx.renderSprite(effect, 0.444444F, getBox().getLeft() + 0.05F + i * gap, getBox().getTop());
            }
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            Map<Holder<MobEffect>, MobEffectInstance> effects = activeEffectsProperty.get();
            int size = effects == null ? 0 : effects.size();
            updateGap(size);
        }

        private void updateGap(float size) {
            gap = Math.min(9.0F, (getBox().getWidth() - 8F) / Math.max(1, size - 1));
        }
    }
}

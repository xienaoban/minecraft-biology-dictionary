package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.TextUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.mixin.MobEffectInstanceIMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class LivingEntityActiveEffectsWidget extends EntityPropertyStandardWidget<LivingEntity> {
    public static final Factory<LivingEntity> FACTORY = LivingEntityActiveEffectsWidget::new;

    private static final int L = 11, T = 3;

    private final CodecProperty<LivingEntity, List<MobEffectInstance>> activeEffectsProperty
            = VanillaEntityProperties.OfLivingEntity.getActiveEffectsProperty(p());

    public LivingEntityActiveEffectsWidget(EntityProperties<LivingEntity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new ActiveEffectsBar());
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        List<MobEffectInstance> effects = activeEffectsProperty.getVal();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (MobEffectInstance effect : effects) {
            int duration = effect.getDuration();
            if (duration > 0) {
                ((MobEffectInstanceIMixin) effect).biologydictionary$setDuration(duration - 1);
            }
        }
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_EFFECTS));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_EFFECTS_DESC));
        list.add(TextUtils.empty());
        List<MobEffectInstance> effects = activeEffectsProperty.getVal();
        if (effects == null || effects.isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            int maxW = -1;
            for (MobEffectInstance effect : effects) {
                Component name = effect.getEffect().value().getDisplayName();
                maxW = Math.max(maxW, ctx.calcTextWidth(name));
            }
            for (MobEffectInstance effect : effects) {
                Component name = TextUtils.concat(Arrays.asList(
                        effect.getEffect().value().getDisplayName(),
                        TextUtils.literal(String.valueOf(effect.getAmplifier() + 1))));
                int duration = effect.getDuration();
                Component time;
                if (ctx.isDebug()) {
                    time = TextUtils.literal(duration + "t").withStyle(ChatFormatting.GRAY);
                } else if (duration == MobEffectInstance.INFINITE_DURATION) {
                    time = TextUtils.translate(Lang.TEXT_INFINITY_CHARACTER).withStyle(ChatFormatting.GRAY);
                } else {
                    time = TextUtils.literal((effect.getDuration() / 20) + "s").withStyle(ChatFormatting.GRAY);
                }
                int w = ctx.calcTextWidth(name) + ctx.calcTextWidth(time);
                Component dot = TextUtils.literal(".".repeat(Math.max(0, (maxW + 40 - w) / 2))).withStyle(ChatFormatting.DARK_GRAY);
                list.add(TextUtils.concat(Arrays.asList(name, dot, time), TextUtils.literal(" ")));
            }
        }
        renderTooltip(ctx, list);
        return true;
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
            List<MobEffectInstance> effects = activeEffectsProperty.getVal();
            if (effects == null || effects.isEmpty()) {
                text = TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS);
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
            for (MobEffectInstance effect : effects) {
                ++i;
                ctx.renderEffect(effect.getEffect(), 0.444444F, getBox().getLeft() + 0.05F + i * gap, getBox().getTop());
            }
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            List<MobEffectInstance> effects = activeEffectsProperty.getVal();
            int size = effects == null ? 0 : effects.size();
            updateGap(size);
        }

        private void updateGap(float size) {
            gap = Math.min(9.0F, (getBox().getWidth() - 8F) / Math.max(1, size - 1));
        }
    }
}

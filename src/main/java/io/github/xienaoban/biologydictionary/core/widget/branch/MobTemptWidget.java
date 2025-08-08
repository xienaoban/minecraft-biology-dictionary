package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.MobTemptProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class MobTemptWidget extends EntityPropertyStandardWidget<Mob> {
    private static final int L = 14, T = 2;

    private final MobTemptProperty temptProperty = p().getExtra(MobTemptProperty.class);

    public MobTemptWidget(EntityProperties<Mob> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new TemptBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_TEMPT),
                tooltipDescription(Lang.PROPERTY_WIDGET_TEMPT_DESC)
        );
        return true;
    }

    private final class TemptBar extends EntityPropertyBar {
        private float gap;
        private int lastSize = 0;

        public TemptBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderFullBar(ctx);
            Component text = null;
            List<ItemStack> tempts = temptProperty.get();
            if (tempts == null) {
                text = Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS);
            } else if (tempts.isEmpty()) {
                text = Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS);
            }
            if (text != null) {
                renderInnerText(ctx, text, Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            if (lastSize != tempts.size()) {
                lastSize = tempts.size();
                updateGap(lastSize);
            }

            for (int i = tempts.size() - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 22 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
            }
            for (int i = tempts.size() - 1; i >= 0; --i) {
                ctx.renderItem(tempts.get(i), 0.5F, getBox().getLeft() + i * gap, getBox().getTop());
            }
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            List<ItemStack> tempts = temptProperty.get();
            int size = tempts == null ? 0 : tempts.size();
            updateGap(size);
        }

        private void updateGap(float size) {
            gap = Math.min(9.0F, (getBox().getWidth() - 8.0F) / Math.max(1, size - 1));
        }
    }
}

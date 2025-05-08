package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.Lang;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.property.extra.MobTemptProperty;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Colors;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MobTemptWidget extends EntityPropertyStandardWidget<Animal> {
    MobTemptProperty mobTemptProperty = p().getExtra(MobTemptProperty.class);

    public MobTemptWidget(EntityProperties<Animal> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 14 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT));
        setElementBar(new TemptBar());
    }

    private final class TemptBar extends EntityPropertyBar {
        private float gap;

        public TemptBar() {
            super(Textures.ICONS, 15 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            List<ItemStack> tempts = mobTemptProperty.get();
            if (tempts == null) {
                renderFullBar(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }
            if (tempts.isEmpty()) {
                renderFullBar(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
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
            List<ItemStack> tempts = mobTemptProperty.get();
            int size = tempts == null ? 0 : tempts.size();
            gap = Math.min(10.0F, (getBox().getWidth() - 8.0F) / Math.max(1, size - 1));
        }
    }
}

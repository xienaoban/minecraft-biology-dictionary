package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Colors;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

@Environment(EnvType.CLIENT)
public final class AnimalFoodWidget extends EntityPropertyStandardWidget<Animal> {
    private final ItemStack[] foods;

    public AnimalFoodWidget(EntityProperties<Animal> properties) {
        super(properties);
        foods = getFoodItems();

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 11 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT));
        setElementBar(new FoodBar());
    }

    private final class FoodBar extends EntityPropertyBar {
        private float gap;

        public FoodBar() {
            super(Textures.ICONS, 12 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            if (foods.length == 0) {
                renderFullBar(ctx);
                renderInnerText(ctx, Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }
            for (int i = foods.length - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 22 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
            }
            for (int i = foods.length - 1; i >= 0; --i) {
                ctx.renderItem(foods[i], 0.5F, getBox().getLeft() + i * gap, getBox().getTop());
            }
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            gap = Math.min(10.0F, (getBox().getWidth() - 8.0F) / Math.max(1, foods.length - 1));
        }
    }

    private ItemStack[] getFoodItems() {
        return BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(itemStack -> e().isFood(itemStack))
                .sorted(Comparator.comparingInt(o -> BuiltInRegistries.ITEM.getId(o.getItem())))
                .toArray(ItemStack[]::new);
    }
}

package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class AnimalFoodWidget extends EntityPropertyStandardWidget<Animal> {
    public static final Factory<Animal> FACTORY = AnimalFoodWidget::new;

    private static final int L = 11, T = 2;

    private final ItemStack[] foods;

    public AnimalFoodWidget(EntityProperties<Animal> properties) {
        super(properties);
        foods = getFoodItems();

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new FoodBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_FOOD));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_FOOD_DESC));
        list.add(TextUtils.empty());

        if (foods.length == 0) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            List<Component> itemNames = new ArrayList<>();
            for (ItemStack food : foods) {
                itemNames.add(food.getHoverName().copy().withStyle(ChatFormatting.WHITE));
            }
            appendWrappedItems(list, ctx, itemNames);
        }
        renderTooltip(ctx, list);
        return true;
    }

    private final class FoodBar extends EntityPropertyBar {
        private float gap;

        public FoodBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderFullBar(ctx);
            if (foods.length == 0) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            for (int i = foods.length - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 21 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
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
        return WorldSession.get().getStaticEntityPropertyCache().getOrCompute(
                e().getType(), AnimalFoodWidget.class, () -> BuiltInRegistries.ITEM.stream()
                        .map(ItemStack::new)
                        .filter(itemStack -> e().isFood(itemStack))
                        .sorted(Comparator.comparingInt(o -> BuiltInRegistries.ITEM.getId(o.getItem())))
                        .toArray(ItemStack[]::new));
    }
}

package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.common.util.LootTableUtils;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.EntityLootTableProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class EntityLootTableWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityLootTableWidget::new;

    private static final int L = 15, T = 2;

    private final EntityLootTableProperty lootTableProperty = p().getExtra(EntityLootTableProperty.class);

    public EntityLootTableWidget(EntityProperties<Entity> properties) {
        super(properties);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new LootItemBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> list = new ArrayList<>();
        list.add(tooltipTitle(Lang.PROPERTY_WIDGET_LOOT_TABLE));
        list.add(tooltipDescription(Lang.PROPERTY_WIDGET_LOOT_TABLE_DESC));
        list.add(Component.empty());

        List<LootTableUtils.LootEntry> entries = lootTableProperty.getVal();
        if (entries == null || entries.isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            // Calculate max width for alignment
            int maxW = -1;
            for (LootTableUtils.LootEntry entry : entries) {
                Component name = entry.getDisplayName();
                maxW = Math.max(maxW, ctx.calcTextWidth(name));
            }

            for (LootTableUtils.LootEntry entry : entries) {
                Component name = entry.getDisplayName();
                Component count = Component.literal(entry.minCount() + "-" + entry.maxCount()).withStyle(ChatFormatting.WHITE);
                Component chance = Component.literal(String.format("%.1f%%", entry.dropChance() * 100)).withStyle(ChatFormatting.GRAY);

                int w = ctx.calcTextWidth(name) + ctx.calcTextWidth(count) + ctx.calcTextWidth(chance);
                Component dot = Component.literal(".".repeat(Math.max(0, (maxW + 40 - w) / 2))).withStyle(ChatFormatting.DARK_GRAY);

                list.add(ComponentUtils.formatList(Arrays.asList(name, dot, count, chance), Component.literal(" ")));
            }
        }

        renderTooltip(ctx, list);
        return true;
    }

    private final class LootItemBar extends EntityPropertyBar {
        private float gap;
        private int lastSize = 0;
        private List<LootTableUtils.LootEntry> lootEntries = List.of();

        public LootItemBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            renderFullBar(ctx);

            List<LootTableUtils.LootEntry> entries = lootTableProperty.getVal();
            if (entries == null) {
                renderInnerText(ctx, Component.translatable(Lang.TEXT_NO_DATA_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }
            if (entries.isEmpty()) {
                renderInnerText(ctx, Component.translatable(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            if (lastSize != entries.size()) {
                lastSize = entries.size();
                lootEntries = entries;
                updateGap(lastSize);
            }

            // Render item icons
            for (int i = lootEntries.size() - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 23 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
            }
            for (int i = lootEntries.size() - 1; i >= 0; --i) {
                ctx.renderItem(new ItemStack(lootEntries.get(i).item()), 0.5F, getBox().getLeft() + i * gap, getBox().getTop());
            }
        }

        @Override
        protected void onResize(int width, int height) {
            super.onResize(width, height);
            List<LootTableUtils.LootEntry> entries = lootTableProperty.getVal();
            int size = (entries == null) ? 0 : entries.size();
            updateGap(size);
        }

        private void updateGap(float size) {
            gap = Math.min(9.0F, (getBox().getWidth() - 8.0F) / Math.max(1, size - 1));
        }
    }
}

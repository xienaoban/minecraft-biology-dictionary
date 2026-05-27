package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.extra.EntityLootTableProperty;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.LootTableUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EntityLootTableWidget extends EntityPropertyStandardWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityLootTableWidget::new;

    private static final int L = 14, T = 3;

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
        list.add(TextUtils.empty());

        List<LootTableUtils.LootEntry> entries = lootTableProperty.getVal();
        if (entries == null || entries.isEmpty()) {
            list.add(tooltipBody(Lang.TEXT_EMPTY_WITH_BRACKETS));
        } else {
            int maxItemNameWidth = 0;
            int maxCountWidth = 0;
            int maxChanceWidth = 0;
            List<List<Component>> columns = new ArrayList<>();
            List<List<Integer>> widths = new ArrayList<>();

            for (LootTableUtils.LootEntry entry : entries) {
                Component itemName = formatItemName(entry);
                int itemNameWidth = ctx.calcTextWidth(itemName);
                maxItemNameWidth = Math.max(maxItemNameWidth, itemNameWidth);

                Component count = formatCount(entry);
                int countWidth = ctx.calcTextWidth(count);
                maxCountWidth = Math.max(maxCountWidth, countWidth);

                Component chance = formatChance(entry);
                int chanceWidth = ctx.calcTextWidth(chance);
                maxChanceWidth = Math.max(maxChanceWidth, chanceWidth);

                Component conditions = formatConditions(entry);

                columns.add(Arrays.asList(itemName, count, chance, conditions));
                widths.add(Arrays.asList(itemNameWidth, countWidth, chanceWidth));
            }

            // Format each entry with alignment
            for (int i = 0; i < entries.size(); i++) {
                List<Component> column = columns.get(i);
                List<Integer> width = widths.get(i);
                MutableComponent dot1 = TextUtils.literal(".".repeat(Math.max(0, (maxItemNameWidth + maxCountWidth - width.get(0) - width.get(1) + 4) / 2))).withStyle(ChatFormatting.DARK_GRAY);
                MutableComponent dot2 = TextUtils.literal(".".repeat(Math.max(0, (maxChanceWidth - width.get(2) + 4) / 2))).withStyle(ChatFormatting.DARK_GRAY);
                list.add(TextUtils.concat(
                        Arrays.asList(column.get(0), dot1, column.get(1), dot2, column.get(2), column.get(3)),
                        TextUtils.space()
                ));
            }
        }

        renderTooltip(ctx, list);
        return true;
    }

    private static Component formatItemName(LootTableUtils.LootEntry entry) {
        return entry.getDisplayName().copy().withStyle(ChatFormatting.WHITE);
    }

    private static Component formatCount(LootTableUtils.LootEntry entry) {
        MutableComponent res;
        if (entry.minCount() < 0 || entry.maxCount() < 0) {
            res = TextUtils.literal("x?");
        } else if (entry.minCount() == entry.maxCount()) {
            res = TextUtils.literal("x" + entry.minCount());
        } else {
            res = TextUtils.literal("x" + entry.minCount() + "-" + entry.maxCount());
        }
        return res.withStyle(ChatFormatting.GRAY);
    }

    private static Component formatChance(LootTableUtils.LootEntry entry) {
        MutableComponent res;
        if (entry.dropChance() < 0) {
            res = TextUtils.literal("?%");
        } else {
            res = TextUtils.literal(String.format("%.1f%%", entry.dropChance() * 100));
        }
        return res.withStyle(ChatFormatting.YELLOW);
    }

    private static Component formatConditions(LootTableUtils.LootEntry entry) {
        if (entry.conditions().isEmpty()) {
            return TextUtils.empty();
        }

        MutableComponent res;
        List<Component> conditions = new ArrayList<>();
        for (Identifier identifier : entry.conditions()) {
            String key = Lang.LOOT_CONDITION_PREFIX + identifier.getNamespace() + '.' + identifier.getPath();
            conditions.add(TextUtils.translate(key));
        }
        MutableComponent inner = TextUtils.concat(conditions, TextUtils.comma());
        res = TextUtils.concat(Arrays.asList(TextUtils.literal("("), inner, TextUtils.literal(")")));
        return res.withStyle(ChatFormatting.GRAY);
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
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }
            if (entries.isEmpty()) {
                renderInnerText(ctx, TextUtils.translate(Lang.TEXT_EMPTY_WITH_BRACKETS), Colors.GRAY_FOR_TEXT_EMPTY);
                return;
            }

            if (lastSize != entries.size()) {
                lastSize = entries.size();
                lootEntries = entries;
                updateGap(lastSize);
            }

            // Render item icons
            for (int i = lootEntries.size() - 1; i >= 0; --i) {
                ctx.renderTexture(Textures.ICONS, 21 * Widget.WIDGET_WIDTH, 2 * Widget.WIDGET_HEIGHT, ctx.getZ(), getBox().getLeft() - 1 + i * gap, getBox().getTop() - 1, 10.0F, 10.0F);
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

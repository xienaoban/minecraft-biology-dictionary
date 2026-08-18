package io.github.xienaoban.biologydictionary.gui.screen;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.EntityOverviewCache;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.biologydictionary.gui.EntityDisplay;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.net.ClientNetManager;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenElementBox;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.FontUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

@ClientOnly
public class BdEntityOverviewScreen extends AbstractBiologyDictionaryScreen {
    private final EntityType<?> entityType;
    private final Entity entity;
    private final EntityProperties<Entity> properties;

    public BdEntityOverviewScreen(EntityDictionaryEntry entry) {
        super(TextUtils.translate(Lang.SCREEN_ENTITY_OVERVIEW_TITLE, entry.getType().getDescription()));
        this.entityType = entry.getType();
        this.entity = createEntity(entry);
        this.properties = entity == null ? null : new EntityProperties<>(entity);

        initBookmarks();
        if (properties != null) {
            initEntityPropertyWidgets();
        } else {
            addAllWidgetsOneByOne(List.of(
                    new UnavailableEntityDisplayWidget(entry),
                    new UnavailableEntityWidget(entry)
            ));
        }
    }

    private static Entity createEntity(EntityDictionaryEntry entry) {
        if (entry.isInstanceCreationFailed()) { return null; }
        try {
            return EntityUtils.create(entry.getType(), ClientUtils.getClientLevel());
        } catch (Throwable e) {
            entry.markInstanceCreationFailed(e);
            return null;
        }
    }

    private void initBookmarks() {
        addBookmarkFromLast(new OpenBdAboutScreenBookmark());
        addBookmarkFromLast(new OpenBdConfigScreenBookmark());
        addBookmark(new OpenBdHomeScreenBookmark());
    }

    private void initEntityPropertyWidgets() {
        List<EntityPropertyWidget<?>> widgets = EntityPropertyWidgets.getWidgets(properties);
        addAllWidgetsOneByOne(widgets);
    }

    public void initOrRequestProperties() {
        if (properties == null) { return; }
        EntityOverviewCache.CacheEntry cache = WorldSession.get().getEntityOverviewCache().get(entityType);
        if (cache != null && cache.isValid()) {
            updateProperties(cache.vanillaNbt(), cache.extraNbt());
        } else {
            // RequestEntityOverviewPacket -> ReplyEntityOverviewPacket -> put cache & updateProperties
            ClientNetManager.requestEntityOverview(entityType);
        }
    }

    public void updateProperties(CompoundTag vanillaNbt, CompoundTag extraNbt) {
        if (entity == null || properties == null) { return; }
        EntityUtils.setNbt(entity, vanillaNbt);
        EntityUtils.setupForDisplay(entity);
        properties.update(vanillaNbt, extraNbt);
    }

    public boolean matchesType(EntityType<?> entityType) {
        return this.entityType == entityType;
    }

    private static final class UnavailableEntityDisplayWidget extends Widget {
        private final EntityDisplay display;

        private UnavailableEntityDisplayWidget(EntityDictionaryEntry entry) {
            super(5, Page.COLUMNS);
            this.display = new EntityDisplay(entry, ClientUtils.getClientLevel());
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            display.renderEntityCentered(ctx, box.getLeft(), box.getTop(), box.getRight(), box.getBottom(),
                    0.0F, 0.0F);
        }
    }

    private static final class UnavailableEntityWidget extends Widget {
        private static final float TEXT_SCALE = 0.5F;
        private static final float V_PADDING = 0F;
        private static final float LINE_SPACING = 1F;
        private static final int MAX_VISIBLE_LINES = 2;
        private static final FormattedCharSequence ELLIPSIS = Component.literal("... ...").getVisualOrderText();

        private final List<FormattedCharSequence> lines;

        private UnavailableEntityWidget(EntityDictionaryEntry entry) {
            super(1, Page.COLUMNS);
            Component description = TextUtils.concat(List.of(
                    TextUtils.literal(entry.getStringId()),
                    TextUtils.translate(Lang.TEXT_ENTITY_CANNOT_OVERVIEW)), Component.literal("\n"));
            this.lines = FontUtils.toLines(description, FontUtils.getGlobalFont(),
                    (int) (Widget.calcWidth(Page.COLUMNS) / TEXT_SCALE));
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            ScreenElementBox box = getBox();
            float lineHeight = FontUtils.getLineHeight(ctx.getFont(), TEXT_SCALE);
            int displayCount = Math.min(lines.size(), MAX_VISIBLE_LINES);
            float availableHeight = box.getHeight() - 2 * V_PADDING;
            float y = box.getTop() + V_PADDING + (availableHeight - displayCount * lineHeight) / 2F;
            float x = box.getLeft();
            for (int i = 0; i < displayCount; i++) {
                ctx.renderText(lines.get(i), Colors.COMMON_DARK_LIGHTER_TEXT, TEXT_SCALE, ctx.getZ(), x, y);
                y += lineHeight + LINE_SPACING;
            }
            if (lines.size() > MAX_VISIBLE_LINES) {
                ctx.renderText(ELLIPSIS, Colors.COMMON_DARK_LIGHTER_TEXT, TEXT_SCALE, ctx.getZ(), x, y);
            }
        }

        @Override
        protected boolean onRenderHovered(ScreenRenderingContext ctx) {
            ScreenElementBox box = getBox();
            float lineHeight = FontUtils.getLineHeight(ctx.getFont(), TEXT_SCALE);
            int displayCount = Math.min(lines.size(), MAX_VISIBLE_LINES);
            float y = box.getTop() + (box.getHeight() - displayCount * lineHeight) / 2F;
            ctx.renderLinedTooltipCentered(lines, TEXT_SCALE, (box.getLeft() + box.getRight()) / 2, y - 2);
            return true;
        }
    }
}

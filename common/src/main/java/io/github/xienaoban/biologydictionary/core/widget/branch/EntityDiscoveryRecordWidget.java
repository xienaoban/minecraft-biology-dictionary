package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.discovery.ClientDiscoveryCache;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoveryRecord;
import io.github.xienaoban.biologydictionary.core.discovery.DiscoverySource;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class EntityDiscoveryRecordWidget extends EntityPropertyWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityDiscoveryRecordWidget::new;

    private static final float TEXT_SCALE = 0.5F;
    private static final float H_PADDING = 1F;
    private static final float V_PADDING = 1F;
    private static final int COLUMNS = Page.COLUMNS;

    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_HOUR = 1000;

    private boolean noRecord;
    private List<FormattedCharSequence> lines;

    private EntityDiscoveryRecordWidget(EntityProperties<Entity> properties) {
        super(properties, 3, COLUMNS);
        ClientDiscoveryCache cache = ClientWorldSession.get().getDiscoveryClientCache();
        DiscoveryRecord record = cache.getRecord(e().getType());
        this.noRecord = (record == null);
        this.lines = buildLines(record);
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        float lineHeight = TextUtils.getLineHeight(ctx.getFont(), TEXT_SCALE);
        float y = getBox().getTop() + V_PADDING;
        float x = getBox().getLeft() + H_PADDING;
        int color = Colors.COMMON_DARK_LIGHTER_TEXT;
        float z = ctx.getZ();

        if (noRecord) {
            ClientDiscoveryCache cache = ClientWorldSession.get().getDiscoveryClientCache();
            DiscoveryRecord record = cache.getRecord(e().getType());
            if (record != null) {
                this.noRecord = false;
                this.lines = buildLines(record);
            }
        }

        for (FormattedCharSequence line : lines) {
            ctx.renderText(line, color, TEXT_SCALE, z, x, y);
            y += lineHeight;
        }
    }

    private static List<FormattedCharSequence> buildLines(DiscoveryRecord record) {
        int maxTextWidth = (int) ((Widget.calcWidth(COLUMNS) - H_PADDING * 2) / TEXT_SCALE);
        Font font = TextUtils.getGlobalFont();
        Component noData = TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS);

        List<FormattedCharSequence> lines = new ArrayList<>(TextUtils.toLines(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_BOND)
                        .withStyle(ChatFormatting.BOLD),
                font, maxTextWidth));

        if (record != null) {
            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_SOURCE).withStyle(ChatFormatting.BOLD),
                getDiscoverySourceText(record.source())
            ), font, maxTextWidth));

            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_REAL_TIME).withStyle(ChatFormatting.BOLD),
                getRealWorldTimeText(record.firstDiscoveryTime())
            ), font, maxTextWidth));

            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_GAME_TIME).withStyle(ChatFormatting.BOLD),
                getGameTimeText(record.firstDiscoveryTick())
            ), font, maxTextWidth));

            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_LOCATION).withStyle(ChatFormatting.BOLD),
                TextUtils.concat(getDimensionText(record), TextUtils.comma(), getBiomeText(record))
            ), font, maxTextWidth));

            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_COORDINATES).withStyle(ChatFormatting.BOLD),
                getCoordinateText(record)
            ), font, maxTextWidth));

            lines.addAll(TextUtils.toLines(TextUtils.concat(
                TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_WEATHER).withStyle(ChatFormatting.BOLD),
                TextUtils.translate("weather." + record.weather().name().toLowerCase())
            ), font, maxTextWidth));
        } else {
            lines.add(noData.getVisualOrderText());
        }

        return lines;
    }

    private static Component getDiscoverySourceText(DiscoverySource source) {
        return switch (source) {
            case ENTITY_DETAIL_SCREEN -> TextUtils.translate(Lang.DISCOVERY_SOURCE_ENTITY_DETAIL_SCREEN);
            case HIGHLIGHT -> TextUtils.translate(Lang.DISCOVERY_SOURCE_HIGHLIGHT);
            case TELESCOPE_OBSERVE -> TextUtils.translate(Lang.DISCOVERY_SOURCE_TELESCOPE_OBSERVE);
            case INTERACT -> TextUtils.translate(Lang.DISCOVERY_SOURCE_INTERACT);
            case KILL -> TextUtils.translate(Lang.DISCOVERY_SOURCE_KILL);
            case KILLED_BY -> TextUtils.translate(Lang.DISCOVERY_SOURCE_KILLED_BY);
            case UNKNOWN -> TextUtils.translate(Lang.DISCOVERY_SOURCE_UNKNOWN);
        };
    }

    private static Component getRealWorldTimeText(long epochMillis) {
        if (epochMillis <= 0) {
            return TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS);
        }
        Instant instant = Instant.ofEpochMilli(epochMillis);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
        return Component.literal(formatter.format(instant));
    }

    private static Component getGameTimeText(long tick) {
        if (tick <= 0) {
            return TextUtils.translate(Lang.TEXT_NO_DATA_WITH_BRACKETS);
        }
        long day = tick / TICKS_PER_DAY + 1;
        long timeOfDay = tick % TICKS_PER_DAY;
        long hours = timeOfDay / TICKS_PER_HOUR;
        long minutes = (timeOfDay % TICKS_PER_HOUR) * 60 / TICKS_PER_HOUR;
        return TextUtils.translate(Lang.PROPERTY_WIDGET_DISCOVERY_GAME_TIME_VALUE, day, hours, minutes);
    }

    private static Component getDimensionText(DiscoveryRecord record) {
        String dimKey = Lang.DIMENSION_PREFIX + record.dimension().getNamespace() + "." + record.dimension().getPath();
        return TextUtils.translate(dimKey);
    }

    private static Component getBiomeText(DiscoveryRecord record) {
        String biomeKey = Lang.BIOME_PREFIX + record.biome().getNamespace() + "." + record.biome().getPath();
        return TextUtils.translate(biomeKey);
    }

    private static Component getCoordinateText(DiscoveryRecord record) {
        net.minecraft.core.BlockPos pos = record.position();
        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
    }
}

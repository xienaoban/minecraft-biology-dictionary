package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyBar;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

@ClientOnly
public final class VillagerScheduleWidget extends EntityPropertyStandardWidget<Villager> {
    public static final Factory<Villager> FACTORY = VillagerScheduleWidget::new;

    private static final int L = 11, T = 4;
    private static final int AW = 4;

    private static final int MAX_TIME = 24000;
    private static final int ZERO_TIME = 18000;

    /**
     * @see net.minecraft.world.timeline.Timelines#bootstrap(net.minecraft.data.worldgen.BootstrapContext)
     */
    private static final TimelineEntry[] EMPTY = {
            new TimelineEntry(0, Activity.IDLE),
    };
    private static final TimelineEntry[] SIMPLE = {
            new TimelineEntry(5000, Activity.WORK),
            new TimelineEntry(11000, Activity.REST),
    };
    private static final TimelineEntry[] BABY = {
            new TimelineEntry(10, Activity.IDLE),
            new TimelineEntry(3000, Activity.PLAY),
            new TimelineEntry(6000, Activity.IDLE),
            new TimelineEntry(10000, Activity.PLAY),
            new TimelineEntry(12000, Activity.REST),
    };
    private static final TimelineEntry[] ADULT_WITH_JOB = {
            new TimelineEntry(10, Activity.IDLE),
            new TimelineEntry(2000, Activity.WORK),
            new TimelineEntry(9000, Activity.MEET),
            new TimelineEntry(11000, Activity.IDLE),
            new TimelineEntry(12000, Activity.REST),
    };
    private static final TimelineEntry[] ADULT_WITHOUT_JOB = {
            new TimelineEntry(10, Activity.IDLE),
            new TimelineEntry(2000, Activity.IDLE),
            new TimelineEntry(9000, Activity.MEET),
            new TimelineEntry(11000, Activity.IDLE),
            new TimelineEntry(12000, Activity.REST),
    };

    private static final TimelineEntry[] EMPTY_ZERO = convertZero(EMPTY);
    private static final TimelineEntry[] SIMPLE_ZERO = convertZero(SIMPLE);
    private static final TimelineEntry[] BABY_ZERO = convertZero(BABY);
    private static final TimelineEntry[] ADULT_WITH_JOB_ZERO = convertZero(ADULT_WITH_JOB);
    private static final TimelineEntry[] ADULT_WITHOUT_JOB_ZERO = convertZero(ADULT_WITHOUT_JOB);

    private static final HashMap<Activity, Integer> textureOffsets = new HashMap<>();

    static {
        textureOffsets.put(Activity.IDLE, 0);
        textureOffsets.put(Activity.WORK, AW);
        textureOffsets.put(Activity.PLAY, AW * 2);
        textureOffsets.put(Activity.MEET, AW * 3);
        textureOffsets.put(Activity.REST, AW * 4);
    }

    private record TimelineEntry(int time, Activity activity) {}

    /**
     * Make 0 represent 00:00 instead of 06:00.
     */
    private static TimelineEntry[] convertZero(TimelineEntry[] source) {
        return Arrays.stream(source)
                .map(e -> new TimelineEntry((e.time() + MAX_TIME - ZERO_TIME) % MAX_TIME, e.activity()))
                .sorted(Comparator.comparingInt(TimelineEntry::time))
                .toArray(TimelineEntry[]::new);
    }

    private static int getTextureOffset(Activity activity) {
        return textureOffsets.getOrDefault(activity, 1);
    }

    private static String tickToTime(long ticks) {
        final int currTicks = ((int) ticks + MAX_TIME - ZERO_TIME) % MAX_TIME;
        final int hour = currTicks / 1000;
        final int minute = (currTicks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hour, minute);
    }

    private static String tickZeroToTime(long ticks) {
        final int currTicks = (int) ticks;
        final int hour = currTicks / 1000;
        final int minute = (currTicks % 1000) * 60 / 1000;
        return String.format("%02d:%02d", hour, minute);
    }

    public VillagerScheduleWidget(EntityProperties<Villager> properties) {
        super(properties, Page.COLUMNS);

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
        setElementBar(new ScheduleBar());
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        renderTooltip(ctx,
                tooltipTitle(Lang.PROPERTY_WIDGET_SCHEDULE),
                tooltipDescription(Lang.PROPERTY_WIDGET_SCHEDULE_DESC)
        );
        return true;
    }

    private final class ScheduleBar extends EntityPropertyBar {

        public ScheduleBar() {
            super(Textures.ICONS, (L + 1) * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT);
        }

        @Override
        protected void onRender(ScreenRenderingContext ctx) {
            super.onRender(ctx);
            final float currTime = Objects.requireNonNull(ctx.getClient().level).getDayTime();
            final float currTimeZero = (currTime + MAX_TIME - ZERO_TIME) % MAX_TIME;
            final TimelineEntry[] timeline = getTimelineEntries();
            final float scale = (getBox().getWidth() - 2) / MAX_TIME;

            int t = 0;
            Activity activity = timeline[timeline.length - 1].activity();
            ctx.renderTexture(getTexture(), getTextureLeft() + getTextureOffset(activity), getTextureTop() + 1, ctx.getZ(), getBox().getLeft(), getBox().getTop() + 1, 1, getBox().getHeight() - 2);
            float hoveredTextLeft = -1;
            int hoveredIdxNext = -1;
            int currTimeIdxCurr = timeline.length - 1;
            for (int i = 0; i < timeline.length; ++i) {
                TimelineEntry entry = timeline[i];

                if (currTimeZero >= entry.time()) {
                    currTimeIdxCurr = i;
                }

                float tl = renderTimelineEntry(ctx, t * scale, (entry.time() - t) * scale, activity);
                if (tl > 0) {
                    hoveredTextLeft = tl;
                    hoveredIdxNext = i;
                }
                t = entry.time();
                activity = entry.activity();
            }
            float tl = renderTimelineEntry(ctx, t * scale, (MAX_TIME - t) * scale, activity);
            if (tl > 0) {
                hoveredTextLeft = tl;
                hoveredIdxNext = 0;
            }
            ctx.renderTexture(getTexture(), getTextureLeft() + getTextureOffset(activity) + AW - 1, getTextureTop() + 1, ctx.getZ(), getBox().getRight() - 1, getBox().getTop() + 1, 1, getBox().getHeight() - 2);

            ctx.renderTexture(getTexture(), 22 * Widget.WIDGET_WIDTH, 3 * Widget.WIDGET_HEIGHT, ctx.getZ(),
                    getBox().getLeft() + 1 + ((currTime + MAX_TIME - ZERO_TIME) % MAX_TIME) * scale - (Widget.WIDGET_WIDTH / 2F) + 1,
                    getBox().getTop() - 1,
                    Widget.WIDGET_WIDTH, Widget.WIDGET_HEIGHT);

            if (hoveredIdxNext < 0) {
                renderText(ctx, timeline, (currTimeIdxCurr + 1) % timeline.length, getBox().getLeft() + 1);
            } else {
                renderText(ctx, timeline, hoveredIdxNext, hoveredTextLeft);
            }
        }

        /**
         * @return positive if it is hovered
         */
        private float renderTimelineEntry(ScreenRenderingContext ctx, float left, float width, Activity activity) {
            final float textureOffset = getTextureOffset(activity);
            final float textureLeft = getTextureLeft() + textureOffset;
            final float offset = getBox().getLeft() + 1;

            final float top = getBox().getTop() - 1;
            final float height = getBox().getHeight() + 2;
            final float right = left + width;

            float currLeft = offset + left;

            final float leftMod = left % AW;
            if (leftMod > 0) {
                float w = Math.min(width, AW - leftMod);
                ctx.renderTexture(getTexture(), textureLeft + leftMod, getTextureTop(), ctx.getZ(), currLeft, top, w, height);
                currLeft += w;
            }
            final float rightMod = right % AW;
            final float rightNoMod = offset + right - rightMod;
            while (currLeft < rightNoMod) {
                ctx.renderTexture(getTexture(), textureLeft, getTextureTop(), ctx.getZ(), currLeft, top, AW, height);
                currLeft += AW;
            }
            if (rightMod > 0) {
                ctx.renderTexture(getTexture(), textureLeft, getTextureTop(), ctx.getZ(), currLeft, top, rightMod, height);
            }

            if (ctx.getMouseX() >= (offset + left) && ctx.getMouseX() < (offset + right)
                    && ctx.getMouseY() >= top && ctx.getMouseY() <= (top + height)) {
                return offset + left;
            }
            return -1;
        }

        private void renderText(ScreenRenderingContext ctx, TimelineEntry[] timeline, int idx, float left) {
            int last = (idx + timeline.length - 1) % timeline.length;

            Component txt1 = TextUtils.translate(Lang.ACTIVITY_PREFIX + timeline[last].activity().getName());
            int w1 = ctx.calcTextWidth(txt1) / 2;

            String str2;
            if (ctx.isDebug()) {
                str2 = (timeline[last].time() + ZERO_TIME) % MAX_TIME + "-" + (timeline[idx].time() + ZERO_TIME) % MAX_TIME;
            } else {
                str2 = tickZeroToTime(timeline[last].time()) + "-" + tickZeroToTime(timeline[idx].time());
            }
            Component txt2 = TextUtils.literal(str2);
            int w2 = ctx.calcTextWidth(txt2) / 2;

            int w = w1 + w2 + 1;

            float l1 = left + 1.0F, l2 = getBox().getRight() - 2.0F - w;
            if (l1 < l2) {
                ctx.renderText(txt1, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), l1, getBox().getTop() + 2 + TXT_TO);
                ctx.renderText(txt2, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), l1 + w1 + 1, getBox().getTop() + 2 + TXT_TO);
            } else {
                ctx.renderText(txt1, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), l2, getBox().getTop() + 2 + TXT_TO);
                ctx.renderText(txt2, Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), l2 + w1 + 1, getBox().getTop() + 2 + TXT_TO);
            }
        }
    }

    private TimelineEntry[] getTimelineEntries() {
        TimelineEntry[] timeline;
        if (e().isBaby()) {
            timeline = BABY_ZERO;
        } else if (Objects.equals(VillagerProfession.NONE,
                e().getVillagerData().profession().unwrapKey().orElse(VillagerProfession.NONE))) {
            timeline = ADULT_WITHOUT_JOB_ZERO;
        } else {
            timeline = ADULT_WITH_JOB_ZERO;
        }
        return timeline;
    }
}

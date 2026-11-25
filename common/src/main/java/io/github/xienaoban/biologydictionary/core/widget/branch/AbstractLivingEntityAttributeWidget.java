package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;

@ClientOnly
public abstract class AbstractLivingEntityAttributeWidget<E extends LivingEntity> extends EntityPropertyStandardWidget<E> {

    public static abstract class Factory<E extends LivingEntity> implements EntityPropertyStandardWidget.Factory<E> {
        @Override
        public final EntityPropertyWidget<E> create(EntityProperties<E> properties) {
            if (properties.entity().getAttributes().hasAttribute(getAttribute())) {
                return create1(properties);
            }
            return null;
        }

        protected abstract Holder<Attribute> getAttribute();
        protected abstract EntityPropertyWidget<E> create1(EntityProperties<E> properties);
    }

    /**
     * Grades of a rolled attribute value. The tooltip color is bright for the dark tooltip background, while
     * the widget color is dark for the light page background.
     */
    protected enum Quality {
        VERY_LOW(0xAAAAAA, 0xEE555555, Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY_VERY_LOW),
        LOW(0x55FF55, 0xEE1E7A1E, Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY_LOW),
        MEDIUM(0x55B4FF, 0xEE0F74AE, Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY_MEDIUM),
        HIGH(0xFF55FF, 0xEEAA00AA, Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY_HIGH),
        VERY_HIGH(0xFFAA00, 0xEE8A6100, Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY_VERY_HIGH);

        private final int tooltipColor;
        private final int widgetColor;
        private final String nameKey;

        Quality(int tooltipColor, int widgetColor, String nameKey) {
            this.tooltipColor = tooltipColor;
            this.widgetColor = widgetColor;
            this.nameKey = nameKey;
        }

        private static Quality of(double ratio) {
            for (int i = 0; i < QUALITY_CUTS.length; ++i) {
                if (ratio < QUALITY_CUTS[i]) { return values()[i]; }
            }
            return VERY_HIGH;
        }

        private Component displayName() {
            return TextUtils.translate(nameKey).withStyle(style -> style.withColor(tooltipColor));
        }
    }

    /**
     * The range an entity rolls this attribute in at spawn time.
     */
    public record RollRange(double min, double max) {
        public double ratioOf(double attr) {
            return (attr - min) / (max - min);
        }
    }

    /**
     * Where a rolled value sits within the range it can be rolled at, relative to the range. Cut evenly by
     * position, so the middle grades are far more likely than the two tails.
     */
    private static final double[] QUALITY_CUTS = {0.2D, 0.4D, 0.6D, 0.8D};

    private final Holder<Attribute> attribute;

    public AbstractLivingEntityAttributeWidget(EntityProperties<E> properties, Holder<Attribute> attribute) {
        super(properties, Page.COLUMNS / 4);
        this.attribute = attribute;
    }

    protected abstract double calcValue(double attr);
    protected abstract String calcUnit(double attr, double value);
    protected abstract String tooltipTitleKey();
    protected abstract String tooltipDescriptionKey();

    /**
     * The range this entity rolls the attribute in, or {@code null} if the attribute is not rolled and thus
     * cannot be graded.
     */
    protected RollRange rollRange() {
        return null;
    }

    protected final Quality quality() {
        RollRange range = rollRange();
        return range == null ? null : Quality.of(range.ratioOf(e().getAttributeValue(attribute)));
    }

    /**
     * Appends the title, the description and, if the attribute is rolled, a blank line and the grade of the
     * current value.
     */
    protected final void appendAttributeTooltip(List<Component> tooltip) {
        tooltip.add(tooltipTitle(tooltipTitleKey()));
        tooltip.add(tooltipDescription(tooltipDescriptionKey()));
        RollRange range = rollRange();
        if (range == null) { return; }
        final double maxAttr = range.max();
        final double maxValue = calcValue(maxAttr);
        tooltip.add(tooltipEmpty());
        tooltip.add(tooltipBody(TextUtils.translate(Lang.PROPERTY_WIDGET_ATTRIBUTE_QUALITY,
                quality().displayName(), StringUtils.format3Digits(maxValue), calcUnit(maxAttr, maxValue))));
    }

    @Override
    protected boolean onRenderHovered(ScreenRenderingContext ctx) {
        List<Component> tooltip = new ArrayList<>(4);
        appendAttributeTooltip(tooltip);
        renderTooltip(ctx, tooltip);
        return true;
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        final double currAttr = e().getAttributeValue(attribute);
        final double currValue = calcValue(currAttr);
        Quality quality = quality();
        renderAttributeText(ctx, currAttr, currValue, calcUnit(currAttr, currValue),
                quality == null ? Colors.COMMON_DARK_TEXT : quality.widgetColor,
                getElementIcon().getBox().getRight() + 1.0F, getBox().getTop());
    }

    /**
     * Renders the value and the unit of an attribute in two lines, to the right of an icon.
     *
     * @param left the left edge of the text
     * @param top the top edge of the widget the attribute belongs to
     */
    protected static void renderAttributeText(ScreenRenderingContext ctx, double attr, double value,
            String unit, int color, float left, float top) {
        String showCurr = StringUtils.format3Digits(BiologyDictionaryClient.isDebugMode() ? attr : value);
        String showUnit = BiologyDictionaryClient.isDebugMode() ? "value" : unit;
        ctx.renderText(TextUtils.literal(showCurr), color, 0.5F, ctx.getZ(),
                left, top + 1 + TXT_ASCII_TO);
        ctx.renderText(TextUtils.literal(showUnit), color, 0.5F, ctx.getZ(),
                left, top + 5 + TXT_ASCII_TO);
    }
}

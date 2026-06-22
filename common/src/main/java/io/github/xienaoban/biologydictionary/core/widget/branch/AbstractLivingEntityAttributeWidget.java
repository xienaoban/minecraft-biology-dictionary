package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.gui.util.Colors;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.StringUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

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

    private final Holder<Attribute> attribute;

    public AbstractLivingEntityAttributeWidget(EntityProperties<E> properties, Holder<Attribute> attribute) {
        super(properties, Page.COLUMNS / 4);
        this.attribute = attribute;
    }

    protected abstract double calcValue(double attr);
    protected abstract String calcUnit(double attr, double value);

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        final double currAttr = e().getAttributeValue(attribute);
        final double currValue = calcValue(currAttr);
        String showCurr = StringUtils.format3Digits(ctx.isDebug() ? currAttr : currValue);
        String showUnit = ctx.isDebug() ? "value" : calcUnit(currAttr, currValue);
        ctx.renderText(TextUtils.literal(showCurr), Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), getElementIcon().getBox().getRight() + 1.0F, getBox().getTop() + 1 + TXT_ASCII_TO);
        ctx.renderText(TextUtils.literal(showUnit), Colors.COMMON_DARK_TEXT, 0.5F, ctx.getZ(), getElementIcon().getBox().getRight() + 1.0F, getBox().getTop() + 5 + TXT_ASCII_TO);
    }
}

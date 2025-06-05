package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.UnsupportedWidgetException;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Colors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

@Environment(EnvType.CLIENT)
public abstract class AbstractLivingEntityAttributeWidget<E extends LivingEntity> extends EntityPropertyStandardWidget<E> {
    private final Holder<Attribute> attribute;

    public AbstractLivingEntityAttributeWidget(EntityProperties<E> properties, Holder<Attribute> attribute) {
        super(properties, 2);
        this.attribute = attribute;
        UnsupportedWidgetException.verify(e().getAttributes().hasAttribute(attribute));
    }

    protected abstract double calcValue(double attr);
    protected abstract String calcUnit(double attr, double value);

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        final double currAttr = e().getAttributeValue(attribute);
        final double currValue = calcValue(currAttr);
        String showCurr = Misc.format3Digits(ctx.isDebug() ? currAttr : currValue);
        String showUnit = ctx.isDebug() ? "value" : calcUnit(currAttr, currValue);
        ctx.renderText(Component.literal(showCurr), Colors.COMMON_DARK_TEXT, 0.5F, getElementIcon().getBox().getRight() + 1.0F, getBox().getTop() + 1.25F);
        ctx.renderText(Component.literal(showUnit), Colors.COMMON_DARK_TEXT, 0.5F, getElementIcon().getBox().getRight() + 1.0F, getBox().getTop() + 5.25F);
    }
}

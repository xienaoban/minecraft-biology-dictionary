package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LivingEntityMovementSpeedWidget extends AbstractLivingEntityAttributeWidget<LivingEntity> {
    private static final int L = 1, T = 1;

    public LivingEntityMovementSpeedWidget(EntityProperties<LivingEntity> properties) {
        super(properties, Attributes.MOVEMENT_SPEED);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }

    @Override
    protected double calcValue(double attr) {
        return 43.178D * attr - 0.02141D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m/s";
    }
}

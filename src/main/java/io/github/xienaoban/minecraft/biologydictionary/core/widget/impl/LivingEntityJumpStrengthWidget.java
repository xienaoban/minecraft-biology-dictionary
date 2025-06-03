package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.Widget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LivingEntityJumpStrengthWidget extends AbstractLivingEntityAttributeWidget<LivingEntity> {
    private static final int L = 1, T = 1;

    public LivingEntityJumpStrengthWidget(EntityProperties<LivingEntity> properties) {
        super(properties, Attributes.JUMP_STRENGTH);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }


    @Override
    protected double calcValue(double attr) {
        return -0.1817584952D * Math.pow(attr, 3) + 3.689713992D * Math.pow(attr, 2) + 2.128599134D * attr - 0.343930367D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m";
    }
}

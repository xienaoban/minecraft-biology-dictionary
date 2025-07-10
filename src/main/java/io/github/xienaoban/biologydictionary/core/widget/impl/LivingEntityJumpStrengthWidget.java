package io.github.xienaoban.biologydictionary.core.widget.impl;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.Widget;
import io.github.xienaoban.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Environment(EnvType.CLIENT)
public final class LivingEntityJumpStrengthWidget extends AbstractLivingEntityAttributeWidget<LivingEntity> {
    private static final int L = 18, T = 2;

    public LivingEntityJumpStrengthWidget(EntityProperties<LivingEntity> properties) {
        super(properties, Attributes.JUMP_STRENGTH);
        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * Widget.WIDGET_WIDTH, T * Widget.WIDGET_HEIGHT));
    }


    @Override
    protected double calcValue(double attr) {
        // The conversion algorithm is from hwyla-addon-horseinfo.
        return -0.1817584952D * Math.pow(attr, 3) + 3.689713992D * Math.pow(attr, 2) + 2.128599134D * attr - 0.343930367D;
    }

    @Override
    protected String calcUnit(double attr, double value) {
        return "m";
    }
}

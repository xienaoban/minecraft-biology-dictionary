package io.github.xienaoban.biologydictionary.core.widget.branch;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.LivingEntity;

@ClientOnly
public final class LivingEntityMovementSpeedWidget extends AbstractMovementSpeedWidget<LivingEntity> {
    public static final EntityPropertyWidget.Factory<LivingEntity> FACTORY =
            factory(LivingEntityMovementSpeedWidget::new);

    public LivingEntityMovementSpeedWidget(EntityProperties<LivingEntity> properties) {
        super(properties);
    }
}

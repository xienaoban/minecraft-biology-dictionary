package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractMovementSpeedWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.animal.equine.Horse;

@ClientOnly
public final class HorseMovementSpeedWidget extends AbstractMovementSpeedWidget<Horse> {
    public static final EntityPropertyWidget.Factory<Horse> FACTORY = factory(HorseMovementSpeedWidget::new);

    /**
     * Vanilla rolls horse speed as the sum of three random doubles, see {@code AbstractHorse#generateSpeed}.
     */
    private static final RollRange RANGE = new RollRange(0.1125D, 0.3375D);

    public HorseMovementSpeedWidget(EntityProperties<Horse> properties) {
        super(properties);
    }

    @Override
    protected RollRange rollRange() {
        return RANGE;
    }
}

package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractMovementSpeedWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.animal.horse.ZombieHorse;

@ClientOnly
public final class ZombieHorseMovementSpeedWidget extends AbstractMovementSpeedWidget<ZombieHorse> {
    public static final EntityPropertyWidget.Factory<ZombieHorse> FACTORY =
            factory(ZombieHorseMovementSpeedWidget::new);

    /**
     * Vanilla rolls zombie horse speed as {@code (9 + r1 + r2 + r3) / 42.16}, see
     * {@code ZombieHorse#generateZombieHorseSpeed}.
     */
    private static final RollRange RANGE = new RollRange(9.0D / 42.16D, 12.0D / 42.16D);

    public ZombieHorseMovementSpeedWidget(EntityProperties<ZombieHorse> properties) {
        super(properties);
    }

    @Override
    protected RollRange rollRange() {
        return RANGE;
    }
}

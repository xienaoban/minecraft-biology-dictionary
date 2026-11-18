package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractJumpStrengthWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.animal.equine.ZombieHorse;

@ClientOnly
public final class ZombieHorseJumpStrengthWidget extends AbstractJumpStrengthWidget<ZombieHorse> {
    public static final EntityPropertyWidget.Factory<ZombieHorse> FACTORY =
            factory(ZombieHorseJumpStrengthWidget::new);

    /**
     * Vanilla rolls zombie horse jump strength as {@code 0.5 + (r1 + r2 + r3) / 15}, see
     * {@code ZombieHorse#generateZombieHorseJumpStrength}.
     */
    private static final RollRange RANGE = new RollRange(0.5D, 0.7D);

    public ZombieHorseJumpStrengthWidget(EntityProperties<ZombieHorse> properties) {
        super(properties);
    }

    @Override
    protected RollRange rollRange() {
        return RANGE;
    }
}

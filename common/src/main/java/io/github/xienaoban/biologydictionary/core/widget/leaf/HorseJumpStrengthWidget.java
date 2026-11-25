package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractJumpStrengthWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.animal.horse.Horse;

@ClientOnly
public final class HorseJumpStrengthWidget extends AbstractJumpStrengthWidget<Horse> {
    public static final EntityPropertyWidget.Factory<Horse> FACTORY = factory(HorseJumpStrengthWidget::new);

    /**
     * Vanilla rolls horse jump strength as the sum of three random doubles, see
     * {@code AbstractHorse#generateJumpStrength}.
     */
    private static final RollRange RANGE = new RollRange(0.4D, 1.0D);

    public HorseJumpStrengthWidget(EntityProperties<Horse> properties) {
        super(properties);
    }

    @Override
    protected RollRange rollRange() {
        return RANGE;
    }
}

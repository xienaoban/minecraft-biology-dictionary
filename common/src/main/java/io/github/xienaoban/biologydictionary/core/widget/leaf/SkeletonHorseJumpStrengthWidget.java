package io.github.xienaoban.biologydictionary.core.widget.leaf;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.widget.branch.AbstractJumpStrengthWidget;
import io.github.xienaoban.biologydictionary.gui.component.property.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;

@ClientOnly
public final class SkeletonHorseJumpStrengthWidget extends AbstractJumpStrengthWidget<SkeletonHorse> {
    public static final EntityPropertyWidget.Factory<SkeletonHorse> FACTORY =
            factory(SkeletonHorseJumpStrengthWidget::new);

    /**
     * Vanilla rolls skeleton horse jump strength as the sum of three random doubles, see
     * {@code AbstractHorse#generateJumpStrength}.
     */
    private static final RollRange RANGE = new RollRange(0.4D, 1.0D);

    public SkeletonHorseJumpStrengthWidget(EntityProperties<SkeletonHorse> properties) {
        super(properties);
    }

    @Override
    protected RollRange rollRange() {
        return RANGE;
    }
}

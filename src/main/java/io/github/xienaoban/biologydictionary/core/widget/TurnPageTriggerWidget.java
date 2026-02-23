package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import net.minecraft.world.entity.Entity;

public final class TurnPageTriggerWidget extends EntityPropertyWidget<Entity> {
    public static final Factory<Entity> FACTORY = TurnPageTriggerWidget::new;

    public TurnPageTriggerWidget() {
        super(null, 1, 1);
    }

    public TurnPageTriggerWidget(EntityProperties<Entity> properties) {
        super(properties, 1, 1);
    }
}

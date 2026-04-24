package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import net.minecraft.world.entity.Entity;

public final class TurnPageCommonWidget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
    public TurnPageCommonWidget() {
        super(null, 1, 1);
    }
}

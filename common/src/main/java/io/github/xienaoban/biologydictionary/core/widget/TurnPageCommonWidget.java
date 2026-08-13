package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.gui.component.Page;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.Entity;

@ClientOnly
public final class TurnPageCommonWidget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
    public TurnPageCommonWidget() {
        super(null, 1, Page.COLUMNS);
    }
}

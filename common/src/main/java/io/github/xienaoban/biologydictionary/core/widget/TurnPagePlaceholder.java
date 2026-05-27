package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import net.minecraft.world.entity.Entity;

public interface TurnPagePlaceholder {

    final class TurnPage1Widget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
        public static final Factory<Entity> FACTORY = TurnPage1Widget::new;
        public TurnPage1Widget(EntityProperties<Entity> properties) { super(properties, 1, 1); }
    }

    final class TurnPage2Widget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
        public static final Factory<Entity> FACTORY = TurnPage2Widget::new;
        public TurnPage2Widget(EntityProperties<Entity> properties) { super(properties, 1, 1); }
    }
}

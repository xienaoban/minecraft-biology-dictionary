package io.github.xienaoban.biologydictionary.core.widget;

import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.world.entity.Entity;

@ClientOnly
public interface TurnPagePlaceholder {

    /**
     * Percent (0~1) of occupied rows that triggers a page turn. 0 = always turn.
     */
    default float getPercent() { return 0f; }

    /**
     * Empty rows left after the soft separation when no page turn happens.
     */
    default int getGapRows() { return 0; }

    final class TurnPage1Widget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
        public static final Factory<Entity> FACTORY = TurnPage1Widget::new;
        public TurnPage1Widget(EntityProperties<Entity> properties) { super(properties, 1, 1); }
        @Override public float getPercent() { return 0.5f; }
    }

    final class TurnPage2Widget extends EntityPropertyWidget<Entity> implements TurnPagePlaceholder {
        public static final Factory<Entity> FACTORY = TurnPage2Widget::new;
        public TurnPage2Widget(EntityProperties<Entity> properties) { super(properties, 1, 1); }
    }
}

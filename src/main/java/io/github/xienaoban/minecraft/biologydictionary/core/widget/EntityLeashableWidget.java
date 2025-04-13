package io.github.xienaoban.minecraft.biologydictionary.core.widget;

import io.github.xienaoban.minecraft.biologydictionary.core.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;

public class EntityLeashableWidget extends EntityPropertyStandardWidget<Entity> {
    private final boolean leashable;

    public EntityLeashableWidget(EntityProperties<Entity> properties) {
        super(properties, 2);
        if (properties.entity() instanceof Leashable entity) {
            this.leashable = (entity.isLeashed() || entity.canBeLeashed());
        } else {
            this.leashable = false;
        }

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, 17 * WIDGET_WIDTH, WIDGET_HEIGHT));
        addElementButton(new LeashableButton());
    }

    private final class LeashableButton extends EntityPropertyButton {

        public LeashableButton() {
            super(Textures.ICONS, 24 * WIDGET_WIDTH, WIDGET_HEIGHT);
            setTextureLeftOffset((leashable ? -1 : 0) * WIDGET_WIDTH);
        }
    }
}

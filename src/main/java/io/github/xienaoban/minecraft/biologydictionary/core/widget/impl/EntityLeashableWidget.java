package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyStandardWidget;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyButton;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import io.github.xienaoban.minecraft.biologydictionary.gui.util.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;

@Environment(EnvType.CLIENT)
public class EntityLeashableWidget extends EntityPropertyStandardWidget<Entity> {
    private static final int L = 17, H = 1;

    private final boolean leashable;

    public EntityLeashableWidget(EntityProperties<Entity> properties) {
        super(properties, 2);
        if (e() instanceof Leashable entity) {
            this.leashable = (entity.isLeashed() || entity.canBeLeashed());
        } else {
            this.leashable = false;
        }

        setElementIcon(new EntityPropertyIcon(Textures.ICONS, L * WIDGET_WIDTH, H * WIDGET_HEIGHT));
        addElementButton(new LeashableButton());
    }

    private final class LeashableButton extends EntityPropertyButton {

        public LeashableButton() {
            super(Textures.ICONS, 23 * WIDGET_WIDTH, WIDGET_HEIGHT);
            setSelectable(false);
            setTextureLeftOffset((leashable ? 0 : 1) * WIDGET_WIDTH);
        }
    }
}

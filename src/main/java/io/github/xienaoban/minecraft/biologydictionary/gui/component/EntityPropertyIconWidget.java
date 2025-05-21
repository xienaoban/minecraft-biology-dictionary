package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityPropertyIconWidget<E extends Entity> extends EntityPropertyWidget<E> {
    private EntityPropertyIcon icon;

    public EntityPropertyIconWidget(EntityProperties<E> properties, int rows, int columns) {
        super(properties, rows, columns);
        this.icon = null;
    }

    public EntityPropertyIcon getElementIcon() { return icon; }
    public void setElementIcon(EntityPropertyIcon icon) {
        updateSubScreenElement(this.icon, icon);
        this.icon = icon;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        if (icon != null) {
            icon.getBox().setPosition(getBox().getLeft(), getBox().getTop());
        }
    }

    protected EntityPropertyIcon geIcon() {
        return icon;
    }
}

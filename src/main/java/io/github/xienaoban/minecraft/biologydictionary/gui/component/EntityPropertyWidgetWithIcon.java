package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityPropertyWidgetWithIcon<E extends Entity> extends EntityPropertyWidget<E> {
    private final EntityPropertyIcon propertyIcon;

    protected EntityPropertyWidgetWithIcon(E entity, RC rowsAndColumns, EntityPropertyIcon propertyIcon) {
        this(entity, rowsAndColumns.rows(), rowsAndColumns.columns(), propertyIcon);
    }

    protected EntityPropertyWidgetWithIcon(E entity, int rows, int columns, EntityPropertyIcon propertyIcon) {
        super(entity, rows, columns);
        propertyIcon.setParent(this);
        this.propertyIcon = propertyIcon;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        propertyIcon.getBox().setPosition(getBox().getLeft(), getBox().getTop());
    }

    protected EntityPropertyIcon geIcon() {
        return propertyIcon;
    }
}

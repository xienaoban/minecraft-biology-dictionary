package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import net.minecraft.world.entity.Entity;

public abstract class EntityPropertyWidgetTemplate1<E extends Entity> extends EntityPropertyWidget<E> {
    private final EntityPropertyIcon propertyIcon;

    protected EntityPropertyWidgetTemplate1(E entity, RC rowsAndColumns, EntityPropertyIcon propertyIcon) {
        super(entity, rowsAndColumns);
        this.propertyIcon = propertyIcon;
    }

    protected EntityPropertyWidgetTemplate1(E entity, int rows, int columns, EntityPropertyIcon propertyIcon) {
        super(entity, rows, columns);
        this.propertyIcon = propertyIcon;
        propertyIcon.setParent(this);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        propertyIcon.getBox().setPosition(getBox().getLeft(), getBox().getTop());
    }
}

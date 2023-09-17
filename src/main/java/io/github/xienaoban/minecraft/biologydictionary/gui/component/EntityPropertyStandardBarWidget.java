package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import net.minecraft.world.entity.Entity;

public class EntityPropertyStandardBarWidget<E extends Entity> extends EntityPropertyWidgetWithIcon<E> {
    private final EntityPropertyBar propertyBar;

    protected EntityPropertyStandardBarWidget(E entity, EntityPropertyIcon propertyIcon, EntityPropertyBar propertyBar) {
        super(entity, 1, Page.COLUMNS / 2, propertyIcon);
        propertyBar.setParent(this);
        this.propertyBar = propertyBar;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        propertyBar.getBox().setPosition(getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1);
    }

    protected EntityPropertyBar getBar() {
        return propertyBar;
    }
}

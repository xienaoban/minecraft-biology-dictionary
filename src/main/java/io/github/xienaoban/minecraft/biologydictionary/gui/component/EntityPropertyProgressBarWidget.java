package io.github.xienaoban.minecraft.biologydictionary.gui.component;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyProgressBar;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.control.EntityPropertyIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityPropertyProgressBarWidget<E extends Entity> extends EntityPropertyIconWidget<E> {
    private final EntityPropertyProgressBar propertyBar;

    protected EntityPropertyProgressBarWidget(E entity, EntityPropertyIcon propertyIcon, EntityPropertyProgressBar propertyBar) {
        super(entity, 1, Page.COLUMNS / 2, propertyIcon);
        propertyBar.setParent(this);
        this.propertyBar = propertyBar;
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        propertyBar.getBox().setPosition(getBox().getLeft() + Widget.WIDGET_WIDTH + 1, getBox().getTop() + 1);
    }

    protected EntityPropertyProgressBar getBar() {
        return propertyBar;
    }
}

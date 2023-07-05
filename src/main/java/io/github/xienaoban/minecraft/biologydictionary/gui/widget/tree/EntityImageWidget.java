package io.github.xienaoban.minecraft.biologydictionary.gui.widget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
public class EntityImageWidget extends EntityWidget<Entity> {

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 2);
        return new RC(6, 1);
    }

    public EntityImageWidget(Entity entity) {
        super(entity, calculateRowsAndColumns(entity));
    }
}

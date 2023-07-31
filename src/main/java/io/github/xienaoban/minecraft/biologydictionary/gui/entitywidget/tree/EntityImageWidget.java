package io.github.xienaoban.minecraft.biologydictionary.gui.entitywidget.tree;

import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
public class EntityImageWidget extends EntityWidget<Entity> {
    private final float entityScale;
    private final float entityBottom;

    public EntityImageWidget(Entity entity) {
        super(entity, calculateRowsAndColumns(entity));
        float[] sp = calculateScaleAndPosition();
        entityScale = sp[0];
        entityBottom = sp[1];
    }

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 5);
        return new RC(5, 3);
    }

    private float[] calculateScaleAndPosition() {
        float entityWidth = (float) entity.getBoundingBox().getXsize();
        float entityHeight = (float) entity.getBoundingBox().getYsize();
        float widgetWidth = getBox().getWidth() - 8;
        float widgetHeight = getBox().getHeight() - 16;
        float scale = Math.min(
                entityWidth < 1 ? widgetWidth / (0.4F * entityWidth + 0.6F) : widgetWidth / entityWidth,
                entityHeight < 1 ? widgetHeight / (0.4F * entityHeight + 0.6F) : widgetHeight / entityHeight
        );
        float bottom = (scale * entityHeight + widgetHeight) / 2 + 10;
        return new float[] { scale, bottom };
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        CommonScreen.renderLivingEntityFollowsMouse(ctx, entity, (getBox().getLeft() + getBox().getRight()) / 2,
                getBox().getTop() + entityBottom, entityScale,
                -ctx.getMouseX() / 20, -ctx.getMouseY() / 20);
    }
}

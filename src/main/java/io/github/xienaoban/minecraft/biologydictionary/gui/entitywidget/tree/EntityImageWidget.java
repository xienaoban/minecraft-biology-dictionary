package io.github.xienaoban.minecraft.biologydictionary.gui.entitywidget.tree;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityWidget;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.EntityApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.CommonScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
public class EntityImageWidget extends EntityWidget<Entity> {

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 5);
        return new RC(5, 3);
    }

    private static Entity createFakeEntity(Entity from) {
        Entity to = from.getType().create(from.level());
        if (to == null) {
            if (from instanceof LocalPlayer me) {
                GameProfile profile = me.getGameProfile();
                to = new RemotePlayer(me.clientLevel, new GameProfile(profile.getId(), profile.getName()));
                // to make name label invisible
                // @see net.minecraft.client.renderer.entity.LivingEntityRenderer.shouldShowName
                Vec3 pos = to.position();
                to.setPos(pos.x(), pos.y() - 4097, pos.z());
            } else {
                to = EntityType.ARMOR_STAND.create(from.level());
            }
        }
        assert to != null;
        updateCompoundTag(from, to);
        return to;
    }

    private static void updateCompoundTag(Entity from, Entity to) {
        CompoundTag tag = new CompoundTag();
        from.saveWithoutId(tag);
        tag.remove("Dimension");
        tag.remove("Rotation");
        tag.remove("CustomName");
        tag.remove("CustomNameVisible");
        tag.remove("AngryAt");
        tag.remove("HurtTime");
        tag.remove("Pos");
        to.load(tag);
        if (to instanceof WaterAnimal waterAnimal) {
            EntityApi.setInWater(waterAnimal, true);
        }
    }

    private final Entity fake;

    private final float entityScale;
    private final float entityBottom;

    public EntityImageWidget(Entity entity) {
        super(entity, calculateRowsAndColumns(entity));
        fake = createFakeEntity(entity);
        float[] sp = calculateScaleAndPosition();
        entityScale = sp[0];
        entityBottom = sp[1];
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
        CommonScreen.renderEntity(ctx, fake, (getBox().getLeft() + getBox().getRight()) / 2,
                getBox().getTop() + entityBottom, entityScale,
                0.06F + (float) Math.atan(ctx.getMouseX() / 40F) / 10,
                0.02F + (float) Math.atan(ctx.getMouseY() / 40F) / 20,
                true);
    }
}

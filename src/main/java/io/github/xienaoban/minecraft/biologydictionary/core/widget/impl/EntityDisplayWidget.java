package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
@Environment(EnvType.CLIENT)
public final class EntityDisplayWidget extends EntityPropertyWidget<Entity> {

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 6);
        return new RC(5, 4);
    }

    private static Entity createModelEntity(Entity entity) {
        Entity model = EntityUtils.create(entity.getType(), entity.level());
        if (model == null) {
            if (entity instanceof LocalPlayer me) {
                GameProfile profile = me.getGameProfile();
                model = new RemotePlayer(me.clientLevel, new GameProfile(profile.getId(), profile.getName()));
                // to make name label invisible
                // @see net.minecraft.client.renderer.entity.LivingEntityRenderer.shouldShowName
                Vec3 pos = model.position();
                model.setPos(pos.x(), pos.y() - 4097, pos.z());
            } else {
                model = EntityUtils.create(EntityType.ARMOR_STAND, entity.level());
            }
        }
        assert model != null;
        updateCompoundTag(entity, model);
        return model;
    }

    private static void updateCompoundTag(Entity from, Entity to) {
        EntityUtils.setNbt(to, EntityUtils.getNbtToDisplay(from));

        // options not controlled by nbt
        if (to instanceof WaterAnimal waterAnimal) {
            EntityUtils.setInWater(waterAnimal, true);
        }
    }

    private final Entity model;

    private final float entityScale;
    private final float entityBottom;

    public EntityDisplayWidget(EntityProperties<Entity> properties) {
        this(properties, createModelEntity(properties.entity()));
    }

    private EntityDisplayWidget(EntityProperties<Entity> properties, Entity model) {
        super(properties, calculateRowsAndColumns(model));
        this.model = model;
        p().setModel(model);
        float[] sp = calculateScaleAndPosition();
        entityScale = sp[0];
        entityBottom = sp[1];
    }

    private float[] calculateScaleAndPosition() {
        float entityWidth = (float) model.getBoundingBox().getXsize();
        float entityHeight = (float) model.getBoundingBox().getYsize();
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
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % 20 == 15 && p().isNotInNoUpdateCooldown()) {
            updateCompoundTag(e(), model);
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        ctx.renderEntityBottomed(model, getBox().getLeft(), getBox().getTop(), getBox().getRight(), getBox().getBottom(),
                0.06F + (float) Math.atan(ctx.getMouseX() / 40F) / 10,
                0.02F + (float) Math.atan(ctx.getMouseY() / 40F) / 20,
                true);
    }
}

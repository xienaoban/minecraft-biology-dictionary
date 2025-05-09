package io.github.xienaoban.minecraft.biologydictionary.core.widget.impl;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.minecraft.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.minecraft.biologydictionary.common.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
@Environment(EnvType.CLIENT)
public final class EntityImageWidget extends EntityPropertyWidget<Entity> {

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 6);
        return new RC(5, 4);
    }

    private static Entity createFakeEntity(Entity entity) {
        Entity fake = EntityUtils.create(entity.getType(), entity.level());
        if (fake == null) {
            if (entity instanceof LocalPlayer me) {
                GameProfile profile = me.getGameProfile();
                fake = new RemotePlayer(me.clientLevel, new GameProfile(profile.getId(), profile.getName()));
                // to make name label invisible
                // @see net.minecraft.client.renderer.entity.LivingEntityRenderer.shouldShowName
                Vec3 pos = fake.position();
                fake.setPos(pos.x(), pos.y() - 4097, pos.z());
            } else {
                fake = EntityUtils.create(EntityType.ARMOR_STAND, entity.level());
            }
        }
        assert fake != null;
        updateCompoundTag(entity, fake);
        return fake;
    }

    private static void updateCompoundTag(Entity from, Entity to) {
        CompoundTag tag = new CompoundTag();
        from.saveWithoutId(tag);
        tag.remove("AngryAt");
        tag.remove("CustomName");
        tag.remove("CustomNameVisible");
        tag.remove("Dimension");
        tag.remove("HurtTime");
        tag.remove("Pos");
        tag.remove("Rotation");

        if (from instanceof LivingEntity) {
            tag.remove("Brain");
            tag.remove("SleepingX");
            tag.remove("SleepingY");
            tag.remove("SleepingZ");
        }

        if (from instanceof AbstractClientPlayer) {
            tag.remove("Inventory");
        } else if (from instanceof Dolphin) {
            tag.remove("GotFish");
        } else if (from instanceof Camel) {
            tag.remove("LastPoseTick");
        }

        to.load(tag);

        // options not controlled by nbt
        if (to instanceof WaterAnimal waterAnimal) {
            EntityUtils.setInWater(waterAnimal, true);
        }
    }

    private final Entity fake;

    private final float entityScale;
    private final float entityBottom;

    public EntityImageWidget(EntityProperties<Entity> properties) {
        this(properties, createFakeEntity(properties.entity()));
    }

    private EntityImageWidget(EntityProperties<Entity> properties, Entity fake) {
        super(properties, calculateRowsAndColumns(fake));
        this.fake = fake;
        float[] sp = calculateScaleAndPosition();
        entityScale = sp[0];
        entityBottom = sp[1];
    }

    private float[] calculateScaleAndPosition() {
        float entityWidth = (float) fake.getBoundingBox().getXsize();
        float entityHeight = (float) fake.getBoundingBox().getYsize();
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
        ctx.renderEntity(fake, (getBox().getLeft() + getBox().getRight()) / 2,
                getBox().getTop() + entityBottom, entityScale,
                0.06F + (float) Math.atan(ctx.getMouseX() / 40F) / 10,
                0.02F + (float) Math.atan(ctx.getMouseY() / 40F) / 20,
                true);
    }
}

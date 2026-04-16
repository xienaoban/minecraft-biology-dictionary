package io.github.xienaoban.biologydictionary.core.widget.branch;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.gui.PlaceholderFallbackEntityRenderer;
import io.github.xienaoban.biologydictionary.gui.component.EntityPropertyWidget;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A widget that displays the target entity. <br/>
 * The entity can be rotated according to the mouse.
 */
@Environment(EnvType.CLIENT)
public final class EntityDisplayWidget extends EntityPropertyWidget<Entity> {
    public static final Factory<Entity> FACTORY = EntityDisplayWidget::new;

    private static RC calculateRowsAndColumns(Entity entity) {
        AABB box = entity.getBoundingBox();
        double x = box.getXsize(), y = box.getYsize();
        if (x > y) return new RC(3, 6);
        return new RC(5, 4);
    }

    private static Entity createModelEntity(EntityProperties<Entity> properties) {
        Entity entity = properties.entity();
        Entity model;
        if (EntityUtils.isFakeEntity(entity)) {
            model = entity;
        } else {
            model = EntityUtils.create(EntityUtils.getEntityType(entity), EntityUtils.getLevel(entity));
        }
        if (model == null) {
            if (entity instanceof LocalPlayer me) {
                GameProfile profile = me.getGameProfile();
                model = new RemotePlayer((ClientLevel) me.level(), new GameProfile(profile.id(), profile.name()));
                // to make name label invisible
                // @see net.minecraft.client.renderer.entity.LivingEntityRenderer.shouldShowName
                Vec3 pos = model.position();
                model.setPos(pos.x(), pos.y() - 4097, pos.z());
            } else {
                model = EntityUtils.create(EntityType.ARMOR_STAND, EntityUtils.getLevel(entity));
            }
        }
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

    private final PlaceholderFallbackEntityRenderer entityRenderer;

    private int leftClickCount;

    public EntityDisplayWidget(EntityProperties<Entity> properties) {
        this(properties, createModelEntity(properties));
    }

    private EntityDisplayWidget(EntityProperties<Entity> properties, Entity model) {
        super(properties, calculateRowsAndColumns(model));
        this.model = model;
        this.entityRenderer = new PlaceholderFallbackEntityRenderer(model);
        p().setModel(model);
    }

    @Override
    protected void onTick(int ticks) {
        super.onTick(ticks);
        if (ticks % ClientUtils.getClientTickCountPerSecond() == 15 && p().isNotInNoUpdateCooldown()) {
            updateCompoundTag(e(), model);
        }
    }

    @Override
    protected void onRender(ScreenRenderingContext ctx) {
        super.onRender(ctx);
        entityRenderer.renderEntityCentered(ctx, getBox().getLeft(), getBox().getTop(), getBox().getRight(), getBox().getBottom(),
                (float) Math.atan(ctx.getMouseX() / 40F) / 10,
                (float) Math.atan(ctx.getMouseY() / 40F) / 20);
    }

    @Override
    protected boolean onMouseDown(float x, float y, int code) {
        Entity entity = e();
        if (isMouseLeft(code)) {
            if (entity instanceof LivingEntity livingEntity) {
                leftClickCount++;
                if (leftClickCount % 5 == 0) {
                    playDeathSound(livingEntity);
                } else {
                    playHurtSound(livingEntity);
                }
            }
        } else if (isMouseRight(code)) {
            if (entity instanceof Mob mob) {
                playAmbientSound(mob);
            }
        }
        return true;
    }

    private void playHurtSound(LivingEntity entity) {
        SoundEvent sound = EntityUtils.getHurtSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }

    private void playDeathSound(LivingEntity entity) {
        SoundEvent sound = EntityUtils.getDeathSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }

    private void playAmbientSound(Mob entity) {
        SoundEvent sound = EntityUtils.getAmbientSound(entity);
        if (sound != null) {
            ClientUtils.playScreenSound(sound, 1.0F, 1.0F);
        }
    }
}

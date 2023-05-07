package io.github.xienaoban.minecraft.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.xienaoban.minecraft.biologydictionary.platform.client.RenderingRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class ShoulderEntityRenderer implements RenderingRegistry.RenderingListener {
    public static void init() {
        RenderingRegistry.registerFirstPersonRendering(new ShoulderEntityRenderer());
    }

    private static final float HEAD_YAW_SPEED = 0.02F;
    private static final float PI_DIV_180 = (float)(Math.PI / 180);

    private final CompoundTag[] oldNBTs = new CompoundTag[2];
    private final LivingEntity[] entities = new LivingEntity[2];
    private final float[] nextHeadYaw = new float[2];
    private final long[] nextHeadYawTime = new long[2];
    private long lastTime;

    @Override
    public void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light) {
        float rx, ry, rz;
        double tx, ty, tz;
        String tmp = "BOTTOM";
        switch (tmp) {
            case "TOP" -> {
                rx = 60 * PI_DIV_180;   tx = 0.5;
                ry = 0 * PI_DIV_180;    ty = 0.0;
                rz = 180 * PI_DIV_180;  tz = -1.7;
            }
            case "BOTTOM" -> {
                rx = 0 * PI_DIV_180;    tx = 0.5;
                ry = 180 * PI_DIV_180;  ty = -1.3;
                rz = 0 * PI_DIV_180;    tz = 1.3;
            }
            case "SIDES" -> {
                rx = -10 * PI_DIV_180;  tx = 2.0;
                ry = 180 * PI_DIV_180;  ty = -0.2;
                rz = 0 * PI_DIV_180;    tz = 1.2;
            }
            default -> { return; }
        }
        long diffTime = System.currentTimeMillis() - this.lastTime;
        this.lastTime += diffTime;
        for (int i = 0; i < 2; ++i) {
            update(player, i == 0 ? player.getShoulderEntityLeft() : player.getShoulderEntityRight(), i);
            LivingEntity entity = this.entities[i];
            if (entity == null) continue;
            if (lastTime > this.nextHeadYawTime[i]) {
                this.nextHeadYawTime[i] = lastTime + 2000 + (long)(Math.random() * 6000);
                this.nextHeadYaw[i] = (float) (Math.random() * 60 - 30);
            }
            entity.setYHeadRot(entity.getYHeadRot() + HEAD_YAW_SPEED * diffTime * (this.nextHeadYaw[i] - entity.getYHeadRot()));
            int pos = i * -2 + 1;
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(rx));
            poseStack.mulPose(Axis.YP.rotation(ry));
            poseStack.mulPose(Axis.ZP.rotation(rz));
            poseStack.translate(pos * tx, ty, tz);
            entityRenderDispatcher.setRenderShadow(false);
            entityRenderDispatcher.render(entity, 0, 0, 0, 0, 1.0F, poseStack, bufferSource, light);
            entityRenderDispatcher.setRenderShadow(true);
            poseStack.popPose();
        }
    }

    private void update(LocalPlayer player, CompoundTag nbt, int index) {
        if (oldNBTs[index] == nbt) return;
        oldNBTs[index] = nbt;
        LivingEntity entity;
        if (nbt == null || nbt.isEmpty()) entity = null;
        else {
            Optional<Entity> optionalEntity = EntityType.create(nbt, player.level);
            if (optionalEntity.isEmpty()) entity = null;
            else {
                entity = (LivingEntity) optionalEntity.get();
                entity.setYRot(0);
                entity.setYHeadRot(0);
                entity.setYBodyRot(0);
                entity.yRotO = 0;
                entity.yHeadRotO = 0;
                entity.yBodyRotO = 0;
                entity.setSpeed(0);
            }
        }
        this.entities[index] = entity;
    }

    public void clear() {
        this.oldNBTs[0] = this.oldNBTs[1] = null;
        this.entities[0] = this.entities[1] = null;
    }
}

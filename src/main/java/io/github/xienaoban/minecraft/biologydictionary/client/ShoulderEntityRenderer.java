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

    private static final float HEAD_ROT_SPEED = 0.02F;

    private final CompoundTag[] nbts = new CompoundTag[2];
    private final LivingEntity[] entities = new LivingEntity[2];
    private final float[] nextYHeadRot = new float[2];
    private final float[] nextXHeadRot = new float[2];
    private final long[] nextHeadYawTime = new long[2];
    private long lastTime;

    @Override
    public void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light) {
        String tmp = "BOTTOM";
        HudPosition hudPos = switch (tmp) {
            case "TOP" -> HudPosition.TOP;
            case "BOTTOM" -> HudPosition.BOTTOM;
            case "SIDES" -> HudPosition.SIDES;
            default -> null;
        };
        if (hudPos == null) return;
        long diffTime = player.getLevel().getGameTime() * 50 + (long) (tickDelta * 50) - this.lastTime;
        this.lastTime += diffTime;
        for (int i = 0; i < 2; ++i) {
            update(player, i == 0 ? player.getShoulderEntityLeft() : player.getShoulderEntityRight(), i);
            LivingEntity entity = this.entities[i];
            if (entity == null) continue;
            if (lastTime > this.nextHeadYawTime[i]) {
                this.nextHeadYawTime[i] = lastTime + 2000 + (long)(Math.random() * 6000);
                this.nextYHeadRot[i] = (float) (Math.random() - 0.5D) * 60F;
                this.nextXHeadRot[i] = (float) (Math.random() - 0.5D) * 20F;
            }
            entity.setYHeadRot(entity.getYHeadRot() + HEAD_ROT_SPEED * diffTime * (this.nextYHeadRot[i] - entity.getYHeadRot()));
            entity.setXRot(entity.getXRot() + HEAD_ROT_SPEED * diffTime * (this.nextXHeadRot[i] - entity.getXRot()));
            int pos = i * -2 + 1;
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotation(hudPos.xRot()));
            poseStack.mulPose(Axis.YP.rotation(hudPos.yRot()));
            poseStack.mulPose(Axis.ZP.rotation(hudPos.zRot()));
            poseStack.translate(pos * hudPos.xPos(), hudPos.yPos() + player.getXRot() * hudPos.yOffset(), hudPos.zPos());
            entityRenderDispatcher.setRenderShadow(false);
            entityRenderDispatcher.render(entity, 0, 0, 0, 0, 1.0F, poseStack, bufferSource, light);
            entityRenderDispatcher.setRenderShadow(true);
            poseStack.popPose();
        }
    }

    private void update(LocalPlayer player, CompoundTag nbt, int index) {
        if (nbts[index] == nbt) return;
        nbts[index] = nbt;
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
        this.nbts[0] = this.nbts[1] = null;
        this.entities[0] = this.entities[1] = null;
    }

    private record HudPosition(float xRot, float yRot, float zRot, double xPos, double yPos, double zPos, float yOffset) {
        private static final float PI_DIV_180 = (float)(Math.PI / 180);

        public static final HudPosition TOP = new HudPosition(60, 0, 180, 0.5, 0.2, -1.8, -0.003F);
        public static final HudPosition BOTTOM = new HudPosition(0, 180, 0, 0.5, -1.4, 1.3, 0.002F);
        public static final HudPosition SIDES = new HudPosition(-10, 180, 0, 1.6, -0.4, 1.0, 0.004F);

        private HudPosition(float xRot, float yRot, float zRot, double xPos, double yPos, double zPos, float yOffset) {
            this.xRot = xRot * PI_DIV_180;
            this.yRot = yRot * PI_DIV_180;
            this.zRot = zRot * PI_DIV_180;
            this.xPos = xPos;
            this.yPos = yPos;
            this.zPos = zPos;
            this.yOffset = yOffset;
        }
    }
}

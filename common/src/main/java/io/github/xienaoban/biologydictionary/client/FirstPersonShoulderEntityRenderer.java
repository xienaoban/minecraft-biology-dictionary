package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * First-person shoulder entity renderer for displaying entities on player's shoulders.
 *
 * @see net.minecraft.client.renderer.ItemInHandRenderer#renderPlayerArm(PoseStack, MultiBufferSource, int, float, float, net.minecraft.world.entity.HumanoidArm)
 * @see net.minecraft.client.renderer.entity.EntityRenderDispatcher#render(net.minecraft.world.entity.Entity, double, double, double, float, float, com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.MultiBufferSource, int)
 */
@ClientOnly
public final class FirstPersonShoulderEntityRenderer {
    private static final float HEAD_ROT_SPEED = 0.02F;

    private final CompoundTag[] nbts = new CompoundTag[2];
    private final LivingEntity[] entities = new LivingEntity[2];
    private final float[] nextYHeadRot = new float[2];
    private final float[] nextXHeadRot = new float[2];
    private final long[] lastHeadYawTime = new long[2];
    private final long[] nextHeadYawTime = new long[2];
    private long lastTime;

    private boolean banned = false;

    /**
     * Renders shoulder entities (e.g., parrots) in first-person view.
     *
     * @param entityRenderDispatcher The entity render dispatcher
     * @param tickDelta The tick delta for interpolation
     * @param poseStack The pose stack for transformations
     * @param bufferSource The buffer source for rendering
     * @param player The local player
     * @param light The light coordinates
     */
    public void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light) {
        if (banned) { return; }
        try {
            run0(entityRenderDispatcher, tickDelta, poseStack, bufferSource, player, light);
        } catch (Exception e) {
            banned = true;
            BiologyDictionaryClient.printThrowableToLoggerAndGame("Failed to render parrots on shoulders", e);
        }
    }

    private void run0(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int light) {
        HudPosition hudPos = switch (ConfigsManager.getClient().getFirstPersonShoulderEntityPosition()) {
            case NONE -> null;
            case TOP -> HudPosition.TOP;
            case BOTTOM -> HudPosition.BOTTOM;
            case SIDES -> HudPosition.SIDES;
        };
        if (hudPos == null) return;

        CompoundTag leftShoulder = player.getShoulderEntityLeft();
        CompoundTag rightShoulder = player.getShoulderEntityRight();
        if ((leftShoulder == null || leftShoulder.isEmpty()) && (rightShoulder == null || rightShoulder.isEmpty())) {
            return;
        }

        long curTime = ClientUtils.getGameTimeMillis(tickDelta);
        long diffTime = Math.min(50, curTime - lastTime);
        lastTime = curTime;

        for (int i = 0; i < 2; ++i) {
            CompoundTag nbt = (i == 0 ? leftShoulder : rightShoulder);
            update(player, nbt, i);
            LivingEntity entity = entities[i];
            if (entity == null) continue;
            if (curTime > nextHeadYawTime[i]) {
                lastHeadYawTime[i] = nextHeadYawTime[i];
                nextHeadYawTime[i] = curTime + 2000 + (long)(Math.random() * 6000);
                nextYHeadRot[i] = (float) (Math.random() - 0.5D) * 60F;
                nextXHeadRot[i] = (float) (Math.random() - 0.5D) * 20F;
            }
            if (curTime - lastHeadYawTime[i] < 1000) {
                float yHeadRotDiff = HEAD_ROT_SPEED * diffTime * (nextYHeadRot[i] - entity.getYHeadRot());
                float xRotDiff     = HEAD_ROT_SPEED * diffTime * (nextXHeadRot[i] - entity.getXRot());
                entity.setYHeadRot(entity.getYHeadRot() + yHeadRotDiff);
                entity.setXRot(    entity.getXRot()     + xRotDiff);
            }
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
        if (nbt == null || nbt.isEmpty()) {
            entity = null;
        } else {
            Entity createdEntity = EntityType.create(nbt, player.level()).orElse(null);
            if (createdEntity == null || !(createdEntity instanceof LivingEntity)) {
                entity = null;
            } else {
                entity = (LivingEntity) createdEntity;
                entity.setYRot(0);
                entity.setYHeadRot(0);
                entity.setYBodyRot(0);
                entity.yRotO = 0;
                entity.yHeadRotO = 0;
                entity.yBodyRotO = 0;
                entity.setSpeed(0);
            }
        }
        entities[index] = entity;
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

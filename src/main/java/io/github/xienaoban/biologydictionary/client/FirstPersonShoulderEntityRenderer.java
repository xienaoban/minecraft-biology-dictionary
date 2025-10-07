package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.xienaoban.biologydictionary.common.client.RenderingRegistry;
import io.github.xienaoban.biologydictionary.common.util.ClientUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class FirstPersonShoulderEntityRenderer implements RenderingRegistry.RenderingListener {
    public static void init() {
        RenderingRegistry.registerFirstPersonRendering(new FirstPersonShoulderEntityRenderer());
    }

    private static final int NULL_VARIANT = -2333333;
    private static final float HEAD_ROT_SPEED = 0.02F;

    private final int[] lrData = { NULL_VARIANT, NULL_VARIANT };
    private final LivingEntity[] entities = new LivingEntity[2];
    private final EntityRenderer<Entity, EntityRenderState>[] entityRenderers = Misc.cast(new EntityRenderer[2]);
    private final EntityRenderState[] entityRenderStates = new EntityRenderState[2];
    private final float[] nextYHeadRot = new float[2];
    private final float[] nextXHeadRot = new float[2];
    private final long[] lastHeadYawTime = new long[2];
    private final long[] nextHeadYawTime = new long[2];
    private long lastTime;

    /**
     * @see net.minecraft.client.renderer.ItemInHandRenderer#renderHandsWithItems(float, com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.SubmitNodeCollector, net.minecraft.client.player.LocalPlayer, int)
     * @see net.minecraft.client.renderer.ItemInHandRenderer#renderPlayerArm(com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.SubmitNodeCollector, int, float, float, net.minecraft.world.entity.HumanoidArm)
     */
    @Override
    public void run(EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light) {
        String tmp = "BOTTOM";
        HudPosition hudPos = switch (tmp) {
            case "TOP" -> HudPosition.TOP;
            case "BOTTOM" -> HudPosition.BOTTOM;
            case "SIDES" -> HudPosition.SIDES;
            default -> null;
        };
        if (hudPos == null) return;
        long curTime = ClientUtils.getGameTimeMillis(tickDelta);
        long diffTime = Math.min(50, curTime - lastTime);
        lastTime = curTime;
        for (int i = 0; i < 2; ++i) {
            update(entityRenderDispatcher, player,
                    (i == 0 ? player.getShoulderParrotLeft() : player.getShoulderParrotRight()), i);
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

            extract(i);
            // [TODO]
            // entityRenderDispatcher.submit(entityRenderStates[i], );

            poseStack.popPose();
        }
    }

    private void update(EntityRenderDispatcher entityRenderDispatcher, LocalPlayer player, Optional<Parrot.Variant> optionalVariant, int index) {
        int variantId = optionalVariant.map(Parrot.Variant::getId).orElse(NULL_VARIANT);
        if (lrData[index] == variantId) return;
        lrData[index] = variantId;
        LivingEntity entity;
        EntityRenderer<Entity, EntityRenderState> entityRenderer;
        EntityRenderState entityRenderState;
        if (variantId == NULL_VARIANT) {
            entity = null;
            entityRenderer = null;
            entityRenderState = null;
        }
        else {
            Parrot parrot = EntityUtils.create(EntityType.PARROT, player.level());
            CompoundTag nbt = VanillaEntityProperties.OfParrot.createVariantProperty().toNbtWith(optionalVariant.get());
            EntityUtils.mergeNbt(parrot, nbt);

            entity = parrot;
            entity.setYRot(0);
            entity.setYHeadRot(0);
            entity.setYBodyRot(0);
            entity.yRotO = 0;
            entity.yHeadRotO = 0;
            entity.yBodyRotO = 0;
            entity.setSpeed(0);

            entityRenderer = EntityUtils.getRenderer(entityRenderDispatcher, entity);
            entityRenderState = EntityUtils.createRenderState(entityRenderer);
        }
        entities[index] = entity;
        entityRenderers[index] = entityRenderer;
        entityRenderStates[index] = entityRenderState;
    }

    private void extract(int index) {
        EntityRenderState entityRenderState = entityRenderStates[index];
        EntityUtils.extractRenderState(entityRenderers[index], entities[index], entityRenderState);

        entityRenderState.lightCoords = 15728880;
        entityRenderState.hitboxesRenderState = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        entityRenderState.leashStates = null;
        entityRenderState.nameTag = null;
    }

    public void clear() {
        lrData[0] = lrData[1] = NULL_VARIANT;
        entities[0] = entities[1] = null;
        entityRenderers[0] = entityRenderers[1] = null;
        entityRenderStates[0] = entityRenderStates[1] = null;
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

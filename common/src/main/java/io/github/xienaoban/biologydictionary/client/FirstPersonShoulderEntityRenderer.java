package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.mixin.rendering.LevelRendererIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import io.github.xienaoban.biologydictionary.platform.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.parrot.Parrot;

import java.util.Optional;

@ClientOnly
public final class FirstPersonShoulderEntityRenderer {
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

    private boolean banned = false;

    public void run(Minecraft client, EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light) {
        if (banned) { return; }
        try {
            run0(client, entityRenderDispatcher, tickDelta, poseStack, submitNodeCollector, player, light);
        } catch (Exception e) {
            banned = true;
            BiologyDictionaryClient.printThrowableToLoggerAndGame("Failed to render parrots on shoulders", e);
        }
    }

    /**
     * @see net.minecraft.client.renderer.ItemInHandRenderer#renderHandsWithItems(float, com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.SubmitNodeCollector, net.minecraft.client.player.LocalPlayer, int)
     * @see net.minecraft.client.renderer.ItemInHandRenderer#renderPlayerArm(com.mojang.blaze3d.vertex.PoseStack, net.minecraft.client.renderer.SubmitNodeCollector, int, float, float, net.minecraft.world.entity.HumanoidArm)
     */
    private void run0(Minecraft client, EntityRenderDispatcher entityRenderDispatcher, float tickDelta, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int light) {
        HudPosition hudPos = switch (ConfigsManager.getClient().getFirstPersonShoulderEntityPosition()) {
            case NONE -> null;
            case TOP -> HudPosition.TOP;
            case BOTTOM -> HudPosition.BOTTOM;
            case SIDES -> HudPosition.SIDES;
        };
        if (hudPos == null) return;

        if (player.getShoulderParrotLeft().isEmpty() && player.getShoulderParrotRight().isEmpty()) {
            return;
        }

        long curTime = ClientUtils.getGameTimeMillis(tickDelta);
        long diffTime = Math.min(50, curTime - lastTime);
        lastTime = curTime;

        PoseStack ps = new PoseStack();
        // @see net.minecraft.client.renderer.ItemInHandRenderer.renderHandsWithItems
        float xp = Mth.lerp(tickDelta, player.xBobO, player.xBob);
        float yp = Mth.lerp(tickDelta, player.yBobO, player.yBob);
        ps.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(tickDelta) - xp) * 0.1F));
        ps.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(tickDelta) - yp) * 0.1F));

        CameraRenderState camera = ((LevelRendererIMixin) client.levelRenderer).biologydictionary$getLevelRenderState().cameraRenderState;

        for (int i = 0; i < 2; ++i) {
            Optional<Parrot.Variant> optionalVariant
                    = i == 0 ? player.getShoulderParrotLeft() : player.getShoulderParrotRight();
            update(entityRenderDispatcher, player, optionalVariant.orElse(null), i);
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
            extract(i);
            EntityRenderState entityRenderState = entityRenderStates[i];
            entityRenderState.lightCoords = light;

            int pos = i * -2 + 1;
            ps.pushPose();
            ps.mulPose(Axis.XP.rotation(hudPos.xRot()));
            ps.mulPose(Axis.YP.rotation(hudPos.yRot()));
            ps.mulPose(Axis.ZP.rotation(hudPos.zRot()));
            ps.translate(pos * hudPos.xPos(), hudPos.yPos() + player.getXRot() * hudPos.yOffset(), hudPos.zPos());
            entityRenderDispatcher.submit(entityRenderState, camera, 0, 0, 0, ps, submitNodeCollector);
            ps.popPose();
        }
    }

    private void update(EntityRenderDispatcher entityRenderDispatcher, LocalPlayer player, Parrot.Variant variant, int index) {
        int variantId = variant == null ? NULL_VARIANT : variant.getId();
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
            Parrot parrot = EntityUtils.create(EntityType.PARROT, EntityUtils.getLevel(player));
            VanillaEntityProperties.OfParrot.createVariantProperty().withVal(variant).setTo(parrot);

            entity = parrot;
            entity.setYRot(0);
            entity.setYHeadRot(0);
            entity.setYBodyRot(0);
            entity.yRotO = 0;
            entity.yHeadRotO = 0;
            entity.yBodyRotO = 0;
            entity.setSpeed(0);

            entityRenderer = RenderUtils.getRenderer(entityRenderDispatcher, entity);
            entityRenderState = RenderUtils.createRenderState(entityRenderer);
        }
        entities[index] = entity;
        entityRenderers[index] = entityRenderer;
        entityRenderStates[index] = entityRenderState;
    }

    private void extract(int index) {
        EntityRenderState entityRenderState = entityRenderStates[index];
        RenderUtils.extractRenderState(entityRenderers[index], entities[index], entityRenderState);
        RenderUtils.renderBodyOnly(entityRenderState);
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

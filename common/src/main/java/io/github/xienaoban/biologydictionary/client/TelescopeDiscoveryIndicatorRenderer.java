package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;

@ClientOnly
public final class TelescopeDiscoveryIndicatorRenderer {
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 11;
    private static final int TEXT_COLOR = 0xFFAAAAAA;

    public static void render(Minecraft client, GuiGraphics guiGraphics) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }

        LocalPlayer player = client.player;
        if (player == null || !player.isScoping()) { return; }

        TelescopeManager telescopeManager = cws.getTelescopeManager();
        int progress = telescopeManager.getDiscoveryProgress();
        boolean completed = telescopeManager.isCompletedDisplay();
        boolean debugMode = BiologyDictionaryClient.isDebugMode();
        if (progress <= 0 && !completed && !debugMode) { return; }

        float centerX = guiGraphics.guiWidth() / 2F;
        float barY = guiGraphics.guiHeight() / 2F + 9;

        ScreenRenderingContext ctx = new ScreenRenderingContext(guiGraphics);

        if (completed) {
            ctx.renderTexture(Textures.GENE, 0, BAR_HEIGHT * 2, 0, centerX - BAR_WIDTH / 2F, barY, BAR_WIDTH, BAR_HEIGHT - 1);
        } else if (progress > 0) {
            // Background: top half of texture (v=0)
            ctx.renderTexture(Textures.GENE, 0, 0, 0,
                    centerX - BAR_WIDTH / 2F, barY, BAR_WIDTH, BAR_HEIGHT - 1);

            // Progress: bottom half of texture (v=16), expand from center
            float progressWidth = (float) progress / TelescopeManager.MAX_PROGRESS * BAR_WIDTH;
            float halfWidth = progressWidth / 2F - 3F;
            float srcMid = (float) Textures.GENE.width() / 2F;

            if (halfWidth > 0) {
                // Right half: from center to right
                ctx.renderTexture(Textures.GENE, srcMid, BAR_HEIGHT, 0,
                        centerX, barY, halfWidth, BAR_HEIGHT - 1);
                // Left half: from center to left
                ctx.renderTexture(Textures.GENE, srcMid - halfWidth, BAR_HEIGHT, 0,
                        centerX - halfWidth, barY, halfWidth, BAR_HEIGHT - 1);
            }
        }

        double dist = getRangingDistance(player, telescopeManager, debugMode);
        if (dist >= 0) {
            Component text = Component.literal(String.format("%.0fm", dist));
            int textY = (int) barY + BAR_HEIGHT - 3;
            guiGraphics.drawCenteredString(client.font, text, (int) centerX, textY, TEXT_COLOR);
        }
    }

    private static double getRangingDistance(LocalPlayer player, TelescopeManager telescopeManager, boolean debugMode) {
        if (debugMode) {
            int range = ConfigsManager.getServer().getTelescopeDiscoveryRange() + 60;
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                    player, entity -> !entity.isSpectator() && entity.isPickable(), range);
            if (hitResult.getType() == HitResult.Type.MISS) { return -1; }
            return player.getEyePosition().distanceTo(hitResult.getLocation());
        }

        Entity target = telescopeManager.getScopingEntity();
        if (target == null) { return -1; }
        return player.getEyePosition().distanceTo(target.getBoundingBox().getCenter());
    }
}

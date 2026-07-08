package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.HitResult;

@ClientOnly
public final class TelescopeDiscoveryIndicatorRenderer {
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 11;
    private static final int TEXT_COLOR = 0xFFAAAAAA;

    public static void render(Minecraft client, GuiGraphicsExtractor guiGraphics) {
        ClientWorldSession cws = ClientWorldSession.get();
        if (cws == null) { return; }

        LocalPlayer player = client.player;
        if (player == null || !player.isScoping()) { return; }

        TelescopeManager telescopeManager = cws.getTelescopeManager();
        int progress = telescopeManager.getDiscoveryProgress();
        boolean completed = telescopeManager.isCompletedDisplay();
        boolean debugMode = BiologyDictionaryClient.isDebugMode();
        if (progress <= 0 && !completed && !debugMode) { return; }

        int centerX = guiGraphics.guiWidth() / 2;
        int barX = centerX - BAR_WIDTH / 2;
        int barY = guiGraphics.guiHeight() / 2 + 9;

        if (completed) {
            blitGene(guiGraphics, barX, barY, 0, BAR_HEIGHT * 2, BAR_WIDTH, BAR_HEIGHT - 1);
        } else if (progress > 0) {
            blitGene(guiGraphics, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT - 1);

            int progressWidth = progress * BAR_WIDTH / TelescopeManager.MAX_PROGRESS;
            int halfWidth = progressWidth / 2 - 3;
            int srcMid = (int) Textures.GENE.width() / 2;

            if (halfWidth > 0) {
                blitGene(guiGraphics, centerX, barY, srcMid, BAR_HEIGHT, halfWidth, BAR_HEIGHT - 1);
                blitGene(guiGraphics, centerX - halfWidth, barY,
                        srcMid - halfWidth, BAR_HEIGHT, halfWidth, BAR_HEIGHT - 1);
            }
        }

        double dist = getRangingDistance(player, telescopeManager, debugMode);
        if (dist >= 0) {
            Component text = Component.literal(String.format("%.0fm", dist));
            int textY = barY + BAR_HEIGHT - 3;
            guiGraphics.centeredText(client.font, text, centerX, textY, TEXT_COLOR);
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

    private static void blitGene(GuiGraphicsExtractor guiGraphics, int x, int y, int u, int v, int width, int height) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Textures.GENE.location(), x, y, u, v, width, height,
                (int) Textures.GENE.width(), (int) Textures.GENE.height());
    }
}

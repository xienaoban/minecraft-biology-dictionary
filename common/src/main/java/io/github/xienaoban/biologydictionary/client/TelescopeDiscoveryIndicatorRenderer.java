package io.github.xienaoban.biologydictionary.client;

import io.github.xienaoban.biologydictionary.core.session.ClientWorldSession;
import io.github.xienaoban.biologydictionary.gui.util.Textures;
import io.github.xienaoban.biologydictionary.platform.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public final class TelescopeDiscoveryIndicatorRenderer {
    private static final int BAR_WIDTH = 32;
    private static final int BAR_HEIGHT = 16;
    private static final int TEXT_COLOR = 0xFFAAAAAA;

    public static void render(Minecraft client, GuiGraphics guiGraphics) {
        ClientWorldSession session = ClientWorldSession.get();
        if (session == null) {
            return;
        }
        LocalPlayer player = client.player;
        if (player == null || !player.isScoping()) {
            return;
        }

        TelescopeManager telescopeManager = session.getTelescopeManager();
        int progress = telescopeManager.getDiscoveryProgress();
        if (progress <= 0) {
            return;
        }

        float centerX = guiGraphics.guiWidth() / 2F;
        float barY = guiGraphics.guiHeight() / 2F + 9;

        ScreenRenderingContext ctx = new ScreenRenderingContext(null);
        ctx.update(guiGraphics, 1f, 1F, 0, 0, 0);

        // Background: top half of texture (v=0)
        ctx.renderTexture(Textures.GENE, 0, 0, 0, centerX - BAR_WIDTH / 2F, barY, BAR_WIDTH, BAR_HEIGHT);

        // Progress: bottom half of texture (v=16), expand from center
        float progressWidth = (float) progress / TelescopeManager.MAX_PROGRESS * BAR_WIDTH;
        float halfWidth = progressWidth / 2F - 3F;
        float srcMid = (float) Textures.GENE.width() / 2F;

        if (halfWidth > 0) {
            // Right half: from center to right
            ctx.renderTexture(Textures.GENE, srcMid, BAR_HEIGHT, 0, centerX, barY, halfWidth, BAR_HEIGHT);
            // Left half: from center to left
            ctx.renderTexture(Textures.GENE, srcMid - halfWidth, BAR_HEIGHT, 0, centerX - halfWidth, barY, halfWidth, BAR_HEIGHT);
        }

        Entity target = telescopeManager.getScopingEntity();
        if (target != null) {
            double dist = player.getEyePosition().distanceTo(target.getBoundingBox().getCenter());
            Component text = Component.literal(String.format("%.0fm", dist));
            int textY = (int) barY + BAR_HEIGHT - 8;
            guiGraphics.drawCenteredString(client.font, text, (int) centerX, textY, TEXT_COLOR);
        }
    }
}

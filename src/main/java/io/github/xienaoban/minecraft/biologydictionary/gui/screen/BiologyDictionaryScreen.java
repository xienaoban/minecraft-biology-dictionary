package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.util.ScreenRenderingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryScreen extends ApiScreen {

    public BiologyDictionaryScreen() {
        super(Component.literal("ABCDEF"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void render(ScreenRenderingContext ctx) {
        renderBackground(ctx.getPoseStack());
        rootScreenElement.render(ctx);
        super.render(ctx);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);
        RenderSystem.setShaderTexture(0, Textures.BOOK);
        blit(poseStack, (width - 192) / 2, (height - 192) / 2, 0, 0, 192, 192);
        blit(poseStack, (width - 192) / 2, (height - 192) / 2, 0, 0, 192, 192);
        renderCenteredText(poseStack, Component.literal(String.valueOf(width)), 0xFFBBBBBB, width * 0.5F, height * 0.5F);
        renderCenteredText(poseStack, Component.literal(String.valueOf(height)), 0xFFBBBBBB, width * 0.5F, height * 0.4F);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.OPEN_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        } else if (KeyMappingManager.TOGGLE_DEBUG.matches(keyCode, scanCode)) {
            screenRenderingContext.setRenderBox(!screenRenderingContext.shouldRenderBox());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

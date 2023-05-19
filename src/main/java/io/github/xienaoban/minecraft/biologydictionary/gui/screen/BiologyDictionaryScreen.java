package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.gui.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryScreen extends ApiScreen {
    public BiologyDictionaryScreen() {
        super(Component.literal("ABCDEF"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, tickDelta);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        super.renderBackground(poseStack);
        RenderSystem.setShaderTexture(0, Textures.BOOK);
        blit(poseStack, (width - 192) / 2, (height - 192) / 2, 0, 0, 192, 192);
        blit(poseStack, (width - 192) / 2, (height - 192) / 2, 0, 0, 192, 192);
        renderCenteredText(poseStack, Component.literal(String.valueOf(width)), width * 0.5F, height * 0.5F, 0xFFBBBBBB);
        renderCenteredText(poseStack, Component.literal(String.valueOf(height)), width * 0.5F, height * 0.4F, 0xFFBBBBBB);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyMappingManager.KM_BIOLOGY_DICTIONARY_SCREEN.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

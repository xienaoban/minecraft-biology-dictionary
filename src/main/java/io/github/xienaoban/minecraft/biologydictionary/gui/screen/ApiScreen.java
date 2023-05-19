package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ApiScreen extends Screen {
    protected ApiScreen(Component component) {
        super(component);
    }

    protected void renderText(PoseStack poseStack, Component component, float x, float y, int color) {
        font.draw(poseStack, component, x, y, color);
    }

    protected void renderCenteredText(PoseStack poseStack, Component component, float x, float y, int color) {
        font.draw(poseStack, component, x - font.width(component) * 0.5F, y, color);
    }
}

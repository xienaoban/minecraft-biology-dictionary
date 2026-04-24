package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.client.TelescopeDiscoveryIndicatorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("TAIL"))
    private void biologydictionary$renderTelescopeDiscoveryIndicator(GuiGraphics guiGraphics, CallbackInfo ci) {
        TelescopeDiscoveryIndicatorRenderer.render(minecraft, guiGraphics);
    }
}

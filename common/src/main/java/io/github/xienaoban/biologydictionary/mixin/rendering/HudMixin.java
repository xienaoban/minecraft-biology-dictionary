package io.github.xienaoban.biologydictionary.mixin.rendering;

import io.github.xienaoban.biologydictionary.client.TelescopeDiscoveryIndicatorRenderer;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;"
            + "Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void biologydictionary$renderTelescopeDiscoveryIndicator(GuiGraphicsExtractor guiGraphics,
                                                                     DeltaTracker deltaTracker, CallbackInfo ci) {
        TelescopeDiscoveryIndicatorRenderer.render(minecraft, guiGraphics);
    }
}

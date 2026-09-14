package io.github.xienaoban.biologydictionary.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.xienaoban.biologydictionary.client.BiologyDictionaryEvent;
import io.github.xienaoban.biologydictionary.client.ClientEvents;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ClientOnly
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
            method = "startUseItem()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled("
                            + "Lnet/minecraft/world/flag/FeatureFlagSet;)Z"),
            cancellable = true)
    private void biologydictionary$useBiologyDictionaryScreen(CallbackInfo ci, @Local InteractionHand interactionHand,
                                                              @Local ItemStack itemStack) {
        if (BiologyDictionaryItem.isBook(itemStack)) {
            Minecraft client = (Minecraft) (Object) this;
            LocalPlayer player = ClientUtils.getClientPlayer(client);
            if (player == null) { return; }
            player.swing(interactionHand);
            BiologyDictionaryEvent.openBookScreen(client);
            ci.cancel();
        }
    }

    /**
     * Timing note for porting to other MC versions:
     * We fire {@link ClientEvents#WORLD_DISCONNECTING} here, at the TAIL of
     * `net.minecraft.client.Minecraft.updateLevelInEngines(
     * net.minecraft.client.multiplayer.ClientLevel, boolean)` with a null level, instead
     * of binding it to the platform DISCONNECT events (Fabric
     * `ClientPlayConnectionEvents.DISCONNECT` / NeoForge `LoggingOut`).
     *
     * Fabric's DISCONNECT fires on the network thread (from `Connection.channelInactive`),
     * concurrent with the render thread still drawing the final frame of the current screen,
     * so any screen that reads the session (e.g. `BdHomeScreen`) can observe a null one and
     * NPE. NeoForge's `LoggingOut` fires on the render thread but still before Minecraft
     * replaces the current screen. This mixin point, by contrast, runs on the render thread
     * and always after `setScreenAndShow` has already swapped the screen, so no screen is
     * rendered after teardown.
     *
     * `updateLevelInEngines` is only invoked from `setLevel` (dimension change, level is
     * non-null) and from `disconnect`/`clearClientLevel` (leaving the world, level is null),
     * so the `level != null` guard keeps dimension changes (overworld/nether/end) intact.
     */
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("TAIL"))
    private void biologydictionary$onWorldLeave(ClientLevel level, boolean stopSound, CallbackInfo ci) {
        if (level != null) { return; }
        for (ClientEvents.ClientListener listener : ClientEvents.WORLD_DISCONNECTING) {
            listener.run((Minecraft) (Object) this);
        }
    }
}

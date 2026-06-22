package io.github.xienaoban.biologydictionary.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.xienaoban.biologydictionary.client.BiologyDictionaryEvent;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.ClientUtils;
import net.minecraft.client.Minecraft;
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
}

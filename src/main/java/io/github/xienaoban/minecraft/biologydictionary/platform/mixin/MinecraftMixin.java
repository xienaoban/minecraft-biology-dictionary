package io.github.xienaoban.minecraft.biologydictionary.platform.mixin;

import io.github.xienaoban.minecraft.biologydictionary.client.BiologyDictionaryEvent;
import io.github.xienaoban.minecraft.biologydictionary.core.BiologyDictionaryItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value= EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique private LocalPlayer player;
    @Unique private InteractionHand hand;
    @Unique private ItemStack handItem;

    @Redirect(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack getHandItem(LocalPlayer player, InteractionHand hand) {
        this.player = player;
        this.hand = hand;
        return this.handItem = player.getItemInHand(hand);
    }


    @Inject(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true)
    private void useBiologyDictionaryScreen(CallbackInfo ci) {
        if (BiologyDictionaryItem.isBook(handItem)) {
            player.swing(hand);
            BiologyDictionaryEvent.openBookScreen((Minecraft) (Object) this);
            ci.cancel();
        }
    }
}

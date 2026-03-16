package io.github.xienaoban.biologydictionary.mixin;

import io.github.xienaoban.biologydictionary.client.BiologyDictionaryEvent;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
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

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique private LocalPlayer biologydictionary$player;
    @Unique private InteractionHand biologydictionary$hand;
    @Unique private ItemStack biologydictionary$handItem;

    @Redirect(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack biologydictionary$getHandItem(LocalPlayer player, InteractionHand hand) {
        biologydictionary$player = player;
        biologydictionary$hand = hand;
        return biologydictionary$handItem = player.getItemInHand(hand);
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true)
    private void biologydictionary$useBiologyDictionaryScreen(CallbackInfo ci) {
        if (BiologyDictionaryItem.isBook(biologydictionary$handItem)) {
            biologydictionary$player.swing(biologydictionary$hand);
            BiologyDictionaryEvent.openBookScreen((Minecraft) (Object) this);
            ci.cancel();
        }
    }
}

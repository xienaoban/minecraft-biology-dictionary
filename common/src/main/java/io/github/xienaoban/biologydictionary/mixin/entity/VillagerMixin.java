package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "updateTrades(Lnet/minecraft/server/level/ServerLevel;)V", at = @At(value = "TAIL"))
    private void biologydictionary$addBiologyDictionaryTrades(ServerLevel serverLevel, CallbackInfo ci) {
        BiologyDictionaryItem.addToMasterLibrarianTrades((Villager) (Object) this);
    }
}

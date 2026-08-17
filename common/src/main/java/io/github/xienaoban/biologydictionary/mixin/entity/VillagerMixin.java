package io.github.xienaoban.biologydictionary.mixin.entity;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerMixin {
    @Inject(method = "updateTrades()V", at = @At(value = "TAIL"))
    private void biologydictionary$addBiologyDictionaryTrades(CallbackInfo ci) {
        BiologyDictionaryItem.addToMasterLibrarianTrades((Villager) (Object) this);
    }
}

package io.github.xienaoban.biologydictionary.mixin.compat;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Attributes the Biology Dictionary book to this mod for Akashic Tome.
 *
 * <p>Akashic Tome keys stored books by their creator mod id and refuses anything whose creator mod
 * is {@code minecraft}. The Biology Dictionary book is a vanilla writable book with custom NBT, so
 * its creator mod resolves to {@code minecraft} and the tome cannot accept it. Returning this mod id
 * for such stacks both lets the tome accept the book and keeps its NBT intact while morphing,
 * because Akashic Tome stores the whole item stack.
 *
 * <p>Akashic Tome is optional, so this is a pseudo mixin and the injector requires nothing: a
 * missing class or a changed method signature must not crash the game.
 */
@Pseudo
@Mixin(targets = "vazkii.akashictome.MorphingHandler", remap = false)
public class AkashicTomeMorphingHandlerMixin {
    @Inject(method = "getModFromStack", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void biologydictionary$getModFromStack(ItemStack stack, CallbackInfoReturnable<String> cir) {
        if (BiologyDictionaryItem.isBook(stack)) {
            cir.setReturnValue(BiologyDictionary.MOD_ID);
        }
    }
}

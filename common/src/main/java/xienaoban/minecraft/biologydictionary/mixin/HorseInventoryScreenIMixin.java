package xienaoban.minecraft.biologydictionary.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(HorseInventoryScreen.class)
public interface HorseInventoryScreenIMixin {
    @Accessor("HORSE_INVENTORY_LOCATION")
    static ResourceLocation getHorseInventoryLocation() {
        throw new AssertionError();
    }
}

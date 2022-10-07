package xienaoban.minecraft.biologydictionary.util;

import net.minecraft.resources.ResourceLocation;
import xienaoban.minecraft.biologydictionary.mixin.HorseInventoryScreenIMixin;

public interface Resources {
    ResourceLocation HORSE_INVENTORY_LOCATION = HorseInventoryScreenIMixin.getHorseInventoryLocation();
}

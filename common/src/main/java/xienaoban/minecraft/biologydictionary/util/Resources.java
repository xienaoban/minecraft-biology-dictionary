package xienaoban.minecraft.biologydictionary.util;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.resources.ResourceLocation;
import xienaoban.minecraft.biologydictionary.mixin.HorseInventoryScreenIMixin;

public interface Resources {
    ResourceLocation BOOK_LOCATION = BookViewScreen.BOOK_LOCATION;
    ResourceLocation HORSE_INVENTORY_LOCATION = HorseInventoryScreenIMixin.getHorseInventoryLocation();
}

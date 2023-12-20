package io.github.xienaoban.minecraft.biologydictionary.net;

import net.minecraft.resources.ResourceLocation;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.MOD_ID;

public final class Channels {
    public static final ResourceLocation REQUEST_BOOK_ITEM = new ResourceLocation(MOD_ID, "request_book_item");
    public static final ResourceLocation REQUEST_ENTITY_DATA = new ResourceLocation(MOD_ID, "request_entity_data");
    public static final ResourceLocation SEND_ENTITY_DATA = new ResourceLocation(MOD_ID, "send_entity_data");
}

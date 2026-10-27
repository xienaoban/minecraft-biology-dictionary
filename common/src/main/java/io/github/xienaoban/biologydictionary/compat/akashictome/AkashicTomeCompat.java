package io.github.xienaoban.biologydictionary.compat.akashictome;

import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Reads the books stored in an Akashic Tome stack.
 *
 * <p>On 1.20.1 Akashic Tome stores the books as item stack NBT, so no reflection is needed.
 * This class is only used after Akashic Tome is detected.
 */
public final class AkashicTomeCompat {
    private static final String TOME_DATA = "akashictome:data";

    private AkashicTomeCompat() {}

    public static boolean isEnabled() {
        return true;
    }

    public static boolean containsBook(ItemStack stack) {
        if (stack == null || stack.isEmpty()) { return false; }
        CompoundTag tag = stack.getTag();
        if (tag == null) { return false; }
        CompoundTag data = tag.getCompound(TOME_DATA);
        for (String key : data.getAllKeys()) {
            if (BiologyDictionaryItem.isBook(ItemStack.of(data.getCompound(key)))) { return true; }
        }
        return false;
    }
}

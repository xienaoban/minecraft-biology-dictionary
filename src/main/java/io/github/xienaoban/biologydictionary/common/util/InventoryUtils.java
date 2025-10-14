package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class InventoryUtils {
    public static boolean hasEnoughItems(Inventory inventory, ItemStack itemStack) {
        return hasEnoughItems(inventory, itemStack, defaultCmp());
    }

    public static boolean hasEnoughItems(Inventory inventory, ItemStack itemStack, BiPredicate<ItemStack, ItemStack> cmp) {
        int left = itemStack.getCount();
        for (ItemStack is : inventory) {
            if (!cmp.test(itemStack, is)) { continue; }
            int cnt = is.getCount();
            left -= cnt;
            if (left <= 0) { return true; }
        }
        return false;
    }

    public static boolean consumeItems(Inventory inventory, ItemStack itemStack) {
        return consumeItems(inventory, itemStack, defaultCmp());
    }

    public static boolean consumeItems(Inventory inventory, ItemStack itemStack, BiPredicate<ItemStack, ItemStack> cmp) {
        int left = itemStack.getCount();
        List<ItemStack> list = inventory.getNonEquipmentItems();
        List<ItemStack> fallback = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            ItemStack is = list.get(i);
            if (!cmp.test(itemStack, is)) { continue; }
            int cnt = is.getCount();
            if (cnt < left) {
                left -= cnt;
                list.set(i, ItemStack.EMPTY);
                fallback.add(is);
                continue;
            }
            if (cnt > left) {
                is.setCount(cnt - left);
            } else {
                list.set(i, ItemStack.EMPTY);
            }
            return true;
        }

        for (ItemStack is : fallback) { inventory.add(is); }
        return false;
    }

    private static BiPredicate<ItemStack, ItemStack> defaultCmp() {
        return (is1, is2) -> Objects.equals(is1.getItem(), is2.getItem());
    }
}

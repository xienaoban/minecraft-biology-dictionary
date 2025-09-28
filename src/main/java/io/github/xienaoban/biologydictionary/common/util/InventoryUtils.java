package io.github.xienaoban.biologydictionary.common.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class InventoryUtils {
    public static boolean hasEnoughItems(Inventory inventory, ItemStack itemStack) {
        final Item target = itemStack.getItem();
        int left = itemStack.getCount();
        for (ItemStack is : inventory) {
            if (!target.equals(is.getItem())) { continue; }
            int cnt = is.getCount();
            left -= cnt;
            if (left <= 0) { return true; }
        }
        return false;
    }

    public static boolean consumeItems(Inventory inventory, ItemStack itemStack) {
        final Item target = itemStack.getItem();
        int left = itemStack.getCount();
        List<ItemStack> list = inventory.getNonEquipmentItems();
        for (int i = 0; i < list.size(); ++i) {
            ItemStack is = list.get(i);
            if (!target.equals(is.getItem())) { continue; }
            int cnt = is.getCount();
            if (cnt < left) {
                left -= cnt;
                list.set(i, ItemStack.EMPTY);
                continue;
            }
            if (cnt > left) {
                is.setCount(cnt - left);
            } else {
                list.set(i, ItemStack.EMPTY);
            }
            return true;
        }
        inventory.add(new ItemStack(target, itemStack.getCount() - left));
        return false;
    }
}

package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @see net.minecraft.world.inventory.HorseInventoryMenu
 */
public class InventoryStealingMenu extends AbstractContainerMenu {
    final Inventory inventory;
    final LivingEntity entity;
    final Container container;
    final EntityInventoryPropertyBundle.InventoryHandler<LivingEntity> handler;

    public InventoryStealingMenu(int containerId, Inventory inventory, LivingEntity entity, Container container) {
        super(null, containerId);
        this.inventory = inventory;
        this.entity = entity;
        this.container = container;
        this.handler = EntityInventoryPropertyBundle.getHandler(entity);

        container.startOpen(inventory.player);

        final int size = container.getContainerSize();
        final int mod = (size % 2 == 0 && size <= 10) ? 2 : 3;
        for (int i = 0; i < size; ++i) {
            this.addSlot(new Slot(container, i, 80 + (i / mod) * 18, 18 + (i % mod) * 18));
        }

        this.addStandardInventorySlots(inventory, 8, 84);
    }

    /**
     * @see net.minecraft.world.inventory.HorseInventoryMenu#quickMoveStack(net.minecraft.world.entity.player.Player, int)
     */
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int j = 2 + this.container.getContainerSize();
            if (i < j) {
                if (!this.moveItemStackTo(itemStack2, j, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).mayPlace(itemStack2) && !this.getSlot(1).hasItem()) {
                if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemStack2) && !this.getSlot(0).hasItem()) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.container.getContainerSize() == 0 || !this.moveItemStackTo(itemStack2, 2, j, false)) {
                int k = j + 27;
                int m = k + 9;
                if (i >= k && i < m) {
                    if (!this.moveItemStackTo(itemStack2, j, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (i >= j && i < k) {
                    if (!this.moveItemStackTo(itemStack2, k, m, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemStack2, k, k, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return (handler == null || handler.getContainer(entity) == this.container)
            && container.stillValid(player)
            && entity.isAlive()
            && player.isWithinEntityInteractionRange(entity, 4.0);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}

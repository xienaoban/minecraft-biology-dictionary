package xienaoban.minecraft.biologydictionary.gui.screen.misc;

import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VillagerInventoryMenu extends AbstractContainerMenu {
    private final Container villagerContainer;
    private final AbstractVillager villager;


    public VillagerInventoryMenu(int syncId, Inventory inventory, Container container, AbstractVillager villager) {
        super(null, syncId);
        this.villagerContainer = container;
        this.villager = villager;
        container.startOpen(inventory.player);
        int i, j;
        for (i = 0; i < villagerContainer.getContainerSize(); ++i) {
            addSlot(new Slot(inventory, i, 80 + (i % 4) * 18, 18 + (i / 4) * 18 + 18));
        }
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18 - 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return villagerContainer.stillValid(player) && villager.isAlive() && villager.distanceToSqr(player) < 64.0F;
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int size = villagerContainer.getContainerSize();
            if (index < size ? !moveItemStackTo(itemStack2, size, slots.size(), true)
                             : !moveItemStackTo(itemStack2, 0, size, false)) return ItemStack.EMPTY;
            if (itemStack2.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.villagerContainer.stopOpen(player);
    }
}

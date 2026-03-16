package io.github.xienaoban.biologydictionary.gui.screen.misc;

import com.mojang.datafixers.util.Pair;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * @see net.minecraft.world.inventory.HorseInventoryMenu
 */
public class InventoryStealingMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT_SLOTS = EquipmentSlot.values().length; // 6 in 1.20.1 (MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD)

    public static final int MAX_SLOTS = 4 * 9;

    private static final ResourceLocation EMPTY_ARMOR_SLOT_SWORD          = ResourceLocation.tryParse("item/empty_slot_sword");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD         = InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
    private static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET         = InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
    private static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE     = InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
    private static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS       = InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
    private static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS          = InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
    // private static final ResourceLocation EMPTY_ARMOR_SLOT_SADDLE;
    // private static final ResourceLocation EMPTY_ARMOR_SLOT_HORSE_ARMOR = ResourceLocation.withDefaultNamespace("gui/sprites/container/horse/armor_slot");
    // private static final ResourceLocation EMPTY_ARMOR_SLOT_LLAMA_ARMOR = ResourceLocation.withDefaultNamespace("gui/sprites/container/horse/llama_armor_slot");

    static int calculateMod(int size) {
        if (size > MAX_SLOTS) {
            return 9;
        }
        int mod;
        if (size % 4 == 0) {
            mod = 4;
        } else if (size % 3 == 0) {
            mod = 3;
        } else if (size % 2 == 0) {
            mod = 2;
        } else {
            return 9;
        }
        return size / mod;
    }

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

        int left = 8, top = 94;
        final int wh = 18;
        addSlot(new EntityEquipmentSlot(EquipmentSlot.HEAD,     left, top, EMPTY_ARMOR_SLOT_HELMET));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.CHEST,    left + wh, top, EMPTY_ARMOR_SLOT_CHESTPLATE));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.LEGS,     left, top += wh, EMPTY_ARMOR_SLOT_LEGGINGS));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.FEET,     left + wh, top, EMPTY_ARMOR_SLOT_BOOTS));
        // SADDLE is not an EquipmentSlot in 1.21.1, ignored
        top += wh;
        // BODY slot doesn't exist in 1.20.1, ignored
        addSlot(new EntityEquipmentSlot(EquipmentSlot.MAINHAND, left, top += wh + 4, EMPTY_ARMOR_SLOT_SWORD));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.OFFHAND,  left + wh, top, EMPTY_ARMOR_SLOT_SHIELD));

        final int size = Math.min(container.getContainerSize(), MAX_SLOTS);
        final int mod = calculateMod(size);
        for (int i = 0; i < size; ++i) {
            this.addSlot(new Slot(container, i, 66 + (i % mod) * wh, 18 + (i / mod) * wh));
        }

        // Add player inventory slots manually (replaces addStandardInventorySlots in 1.21.1)
        for (int m = 0; m < 3; m++) {
            for (int n = 0; n < 9; n++) {
                this.addSlot(new Slot(inventory, n + m * 9 + 9, 66 + n * 18, 130 + m * 18 - 18));
            }
        }

        for (int m = 0; m < 9; m++) {
            this.addSlot(new Slot(inventory, m, 66 + m * 18, 170));
        }
    }

    /**
     * @see net.minecraft.world.inventory.HorseInventoryMenu#quickMoveStack(net.minecraft.world.entity.player.Player, int)
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            // Equipment slots (7) + Saddle slot (1) + container slots
            int containerEnd = EQUIPMENT_SLOTS + 1 + Math.min(container.getContainerSize(), MAX_SLOTS);
            if (index < containerEnd) {
                return ItemStack.EMPTY;
            } else {
                int k = containerEnd + 27;
                int m = k + 9;
                if (index >= k && index < m) {
                    if (!this.moveItemStackTo(itemStack2, containerEnd, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < k) {
                    if (!this.moveItemStackTo(itemStack2, k, m, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemStack2, k, k, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return (handler == null || handler.getContainer(entity) == container)
            && container.stillValid(player)
            && entity.isAlive()
            && player.distanceToSqr(entity) <= 16.0; // 4.0 blocks, squared
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    public Player getPlayer() {
        return inventory.player;
    }

    /**
     * Container wrapper for entity equipment slots
     */
    class EntityEquipmentContainer implements Container {
        private final EquipmentSlot equipmentSlot;

        EntityEquipmentContainer(EquipmentSlot equipmentSlot) {
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return entity.getItemBySlot(equipmentSlot).isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? entity.getItemBySlot(equipmentSlot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot == 0) {
                ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                    return itemStack;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot == 0) {
                ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                    return itemStack;
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack itemStack) {
            if (slot == 0) {
                entity.setItemSlot(equipmentSlot, itemStack);
                if (!itemStack.isEmpty() && entity instanceof Mob mob) {
                    mob.setGuaranteedDrop(equipmentSlot);
                    mob.setPersistenceRequired();
                }
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return entity.isAlive() && player.distanceToSqr(entity) <= 16.0; // 4.0 blocks, squared
        }

        @Override
        public void clearContent() {
            entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
    }

    /**
     * Slot wrapper for entity equipment
     */
    class EntityEquipmentSlot extends Slot {
        private final EquipmentSlot equipmentSlot;
        private final ResourceLocation emptyIcon;

        EntityEquipmentSlot(EquipmentSlot equipmentSlot, int x, int y, ResourceLocation emptyIcon) {
            super(new EntityEquipmentContainer(equipmentSlot), 0, x, y);
            this.equipmentSlot = equipmentSlot;
            this.emptyIcon = emptyIcon;
        }

        @Override
        public void setByPlayer(ItemStack newStack) {
            ItemStack oldStack = getItem();
            entity.onEquipItem(equipmentSlot, oldStack, newStack);
            super.setByPlayer(newStack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            if (!PlayerUtils.isCreative(getPlayer())) {
                return false;
            }
            return equipmentSlot == entity.getEquipmentSlotForItem(itemStack);
        }

        @Override
        public boolean mayPickup(Player player) {
            if (!PlayerUtils.isCreative(player)) {
                return false;
            }
            ItemStack itemStack = getItem();
            if (!itemStack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemStack) > 0) {
                return false;
            }
            return super.mayPickup(player);
        }

        @Override
        public boolean isActive() {
            // return entity.canUseSlot(equipmentSlot);
            return true;
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon) : null;
        }
    }
}

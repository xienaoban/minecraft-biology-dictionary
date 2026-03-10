package io.github.xienaoban.biologydictionary.gui.screen.misc;

import com.mojang.datafixers.util.Pair;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.ticks.ContainerSingleItem;

/**
 * @see net.minecraft.world.inventory.HorseInventoryMenu
 */
public class InventoryStealingMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT_SLOTS = EquipmentSlot.values().length; // 7 in 1.21.1 (MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD, BODY)

    public static final int MAX_SLOTS = 4 * 9;

    private static final ResourceLocation EMPTY_ARMOR_SLOT_SWORD          = ResourceLocation.withDefaultNamespace("container/slot/sword");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD         = ResourceLocation.withDefaultNamespace("container/slot/shield");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET         = ResourceLocation.withDefaultNamespace("container/slot/helmet");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE     = ResourceLocation.withDefaultNamespace("container/slot/chestplate");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS       = ResourceLocation.withDefaultNamespace("container/slot/leggings");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS          = ResourceLocation.withDefaultNamespace("container/slot/boots");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_SADDLE         = ResourceLocation.withDefaultNamespace("container/slot/saddle");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_HORSE_ARMOR    = ResourceLocation.withDefaultNamespace("container/slot/horse_armor");
    private static final ResourceLocation EMPTY_ARMOR_SLOT_LLAMA_ARMOR    = ResourceLocation.withDefaultNamespace("container/slot/llama_armor");

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

        ResourceLocation emptyBodySlot = switch (entity) {
            case Llama ignored -> EMPTY_ARMOR_SLOT_LLAMA_ARMOR;
            default -> EMPTY_ARMOR_SLOT_HORSE_ARMOR;
        };
        int left = 8, top = 94;
        final int wh = 18;
        addSlot(new EntityEquipmentSlot(EquipmentSlot.HEAD,     left, top, EMPTY_ARMOR_SLOT_HELMET));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.CHEST,    left + wh, top, EMPTY_ARMOR_SLOT_CHESTPLATE));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.LEGS,     left, top += wh, EMPTY_ARMOR_SLOT_LEGGINGS));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.FEET,     left + wh, top, EMPTY_ARMOR_SLOT_BOOTS));
        // SADDLE is not an EquipmentSlot in 1.21.1, use a special Slot instead
        addSlot(new SaddleSlot(container, 0, left, top += wh, EMPTY_ARMOR_SLOT_SADDLE));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.BODY,     left + wh, top, emptyBodySlot));
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
                this.addSlot(new Slot(inventory, n + m * 9 + 9, 8 + n * 18, 102 + m * 18 - 18));
            }
        }

        for (int m = 0; m < 9; m++) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
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
            && player.canInteractWithEntity(entity, 4.0);
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
    class EntityEquipmentContainer implements ContainerSingleItem {
        private final EquipmentSlot equipmentSlot;

        EntityEquipmentContainer(EquipmentSlot equipmentSlot) {
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public ItemStack getTheItem() {
            return entity.getItemBySlot(equipmentSlot);
        }

        @Override
        public void setTheItem(ItemStack itemStack) {
            entity.setItemSlot(equipmentSlot, itemStack);
            if (!itemStack.isEmpty() && entity instanceof Mob mob) {
                mob.setGuaranteedDrop(equipmentSlot);
                mob.setPersistenceRequired();
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
            return entity.isAlive() && player.canInteractWithEntity(entity, 4.0);
        }
    }

    /**
     * Slot wrapper for entity equipment
     * @see net.minecraft.world.inventory.ArmorSlot
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
        public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
            entity.onEquipItem(equipmentSlot, oldStack, newStack);
            super.setByPlayer(newStack, oldStack);
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
            if (!itemStack.isEmpty() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
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

    /**
     * Special slot for saddle (SADDLE is not an EquipmentSlot in 1.21.1)
     * @see net.minecraft.world.inventory.HorseInventoryMenu
     */
    class SaddleSlot extends Slot {
        private final ResourceLocation emptyIcon;

        SaddleSlot(Container container, int slot, int x, int y, ResourceLocation emptyIcon) {
            super(container, slot, x, y);
            this.emptyIcon = emptyIcon;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            if (!PlayerUtils.isCreative(getPlayer())) {
                return false;
            }
            // Check if entity can have saddle (Horse, Llama, etc.)
            boolean canSaddle = entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse
                    && ((net.minecraft.world.entity.animal.horse.AbstractHorse) entity).isSaddleable();
            return canSaddle && itemStack.is(net.minecraft.world.item.Items.SADDLE);
        }

        @Override
        public boolean isActive() {
            return entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse
                    && ((net.minecraft.world.entity.animal.horse.AbstractHorse) entity).isSaddleable();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon) : null;
        }
    }
}

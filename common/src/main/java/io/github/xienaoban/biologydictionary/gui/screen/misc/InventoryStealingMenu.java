package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.ticks.ContainerSingleItem;

/**
 * @see net.minecraft.world.inventory.HorseInventoryMenu
 */
public class InventoryStealingMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT_SLOTS = EquipmentSlot.values().length;
    public static final int MAX_SLOTS = 4 * 9;

    private static final Identifier EMPTY_ARMOR_SLOT_SWORD          = Identifier.withDefaultNamespace("container/slot/sword");
    private static final Identifier EMPTY_ARMOR_SLOT_SHIELD         = Identifier.withDefaultNamespace("container/slot/shield");
    private static final Identifier EMPTY_ARMOR_SLOT_HELMET         = Identifier.withDefaultNamespace("container/slot/helmet");
    private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE     = Identifier.withDefaultNamespace("container/slot/chestplate");
    private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS       = Identifier.withDefaultNamespace("container/slot/leggings");
    private static final Identifier EMPTY_ARMOR_SLOT_BOOTS          = Identifier.withDefaultNamespace("container/slot/boots");
    private static final Identifier EMPTY_ARMOR_SLOT_SADDLE         = Identifier.withDefaultNamespace("container/slot/saddle");
    private static final Identifier EMPTY_ARMOR_SLOT_HORSE_ARMOR    = Identifier.withDefaultNamespace("container/slot/horse_armor");
    private static final Identifier EMPTY_ARMOR_SLOT_LLAMA_ARMOR    = Identifier.withDefaultNamespace("container/slot/llama_armor");
    private static final Identifier EMPTY_ARMOR_SLOT_NAUTILUS_ARMOR = Identifier.withDefaultNamespace("container/slot/nautilus_armor_inventory");

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

        Identifier emptyBodySlot = switch (entity) {
            case Nautilus ignored -> EMPTY_ARMOR_SLOT_NAUTILUS_ARMOR;
            case Llama ignored -> EMPTY_ARMOR_SLOT_LLAMA_ARMOR;
            default -> EMPTY_ARMOR_SLOT_HORSE_ARMOR;
        };
        int left = 8, top = 94;
        final int wh = 18;
        addSlot(new EntityEquipmentSlot(EquipmentSlot.HEAD,     left, top, EMPTY_ARMOR_SLOT_HELMET));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.CHEST,    left + wh, top, EMPTY_ARMOR_SLOT_CHESTPLATE));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.LEGS,     left, top += wh, EMPTY_ARMOR_SLOT_LEGGINGS));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.FEET,     left + wh, top, EMPTY_ARMOR_SLOT_BOOTS));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.SADDLE,   left, top += wh, EMPTY_ARMOR_SLOT_SADDLE));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.BODY,     left + wh, top, emptyBodySlot));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.MAINHAND, left, top += wh + 4, EMPTY_ARMOR_SLOT_SWORD));
        addSlot(new EntityEquipmentSlot(EquipmentSlot.OFFHAND,  left + wh, top, EMPTY_ARMOR_SLOT_SHIELD));

        final int size = Math.min(container.getContainerSize(), MAX_SLOTS);
        final int mod = calculateMod(size);
        for (int i = 0; i < size; ++i) {
            this.addSlot(new Slot(container, i, 66 + (i % mod) * wh, 18 + (i / mod) * wh));
        }

        this.addStandardInventorySlots(inventory, 66, 112);
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
            int containerEnd = EQUIPMENT_SLOTS + Math.min(container.getContainerSize(), MAX_SLOTS);
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
            && player.isWithinEntityInteractionRange(entity, 4.0);
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
            return entity.isAlive() && player.isWithinEntityInteractionRange(entity, 4.0);
        }
    }

    /**
     * Slot wrapper for entity equipment
     * @see net.minecraft.world.inventory.ArmorSlot
     */
    class EntityEquipmentSlot extends Slot {
        private final EquipmentSlot equipmentSlot;
        private final Identifier emptyIcon;

        EntityEquipmentSlot(EquipmentSlot equipmentSlot, int x, int y, Identifier emptyIcon) {
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
            return entity.isEquippableInSlot(itemStack, equipmentSlot);
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
        public Identifier getNoItemIcon() {
            return emptyIcon;
        }
    }
}

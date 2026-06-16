package io.github.xienaoban.biologydictionary.gui.screen.misc;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityInventoryPropertyBundle;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.PlayerUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.ticks.ContainerSingleItem;

public class InventoryStealingMenu extends AbstractContainerMenu {
	public static final int EQUIPMENT_SLOTS = EquipmentSlot.values().length;
	public static final int MAX_SLOTS = 4 * 9;

	private static final Identifier EMPTY_ARMOR_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");
	private static final Identifier EMPTY_ARMOR_SLOT_SHIELD = InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
	private static final Identifier EMPTY_ARMOR_SLOT_HELMET = InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
	private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE = InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
	private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS = InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
	private static final Identifier EMPTY_ARMOR_SLOT_BOOTS = InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
	private static final Identifier EMPTY_ARMOR_SLOT_SADDLE = Identifier.withDefaultNamespace("container/slot/saddle");
	private static final Identifier EMPTY_ARMOR_SLOT_HORSE_ARMOR = Identifier.withDefaultNamespace("container/slot/horse_armor");
	private static final Identifier EMPTY_ARMOR_SLOT_LLAMA_ARMOR = Identifier.withDefaultNamespace("container/slot/llama_armor");
	private static final Identifier EMPTY_ARMOR_SLOT_NAUTILUS_ARMOR =
			Identifier.withDefaultNamespace("container/slot/nautilus_armor_inventory");

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

	private boolean closedByDistance = false;

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
		int left = 8;
		int top = 94;
		final int wh = 18;
		addSlot(new EntityEquipmentSlot(EquipmentSlot.HEAD, left, top, EMPTY_ARMOR_SLOT_HELMET));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.CHEST, left + wh, top, EMPTY_ARMOR_SLOT_CHESTPLATE));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.LEGS, left, top += wh, EMPTY_ARMOR_SLOT_LEGGINGS));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.FEET, left + wh, top, EMPTY_ARMOR_SLOT_BOOTS));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.SADDLE, left, top += wh, EMPTY_ARMOR_SLOT_SADDLE));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.BODY, left + wh, top, emptyBodySlot));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.MAINHAND, left, top += wh + 4, EMPTY_ARMOR_SLOT_SWORD));
		addSlot(new EntityEquipmentSlot(EquipmentSlot.OFFHAND, left + wh, top, EMPTY_ARMOR_SLOT_SHIELD));

		final int size = Math.min(container.getContainerSize(), MAX_SLOTS);
		final int mod = calculateMod(size);
		for (int i = 0; i < size; ++i) {
			addSlot(new Slot(container, i, 66 + (i % mod) * wh, 18 + (i / mod) * wh));
		}

		addStandardInventorySlots(inventory, 66, 112);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack = slot.getItem();
			int containerEnd = EQUIPMENT_SLOTS + Math.min(container.getContainerSize(), MAX_SLOTS);
			if (index < containerEnd) {
				return ItemStack.EMPTY;
			}
			int inventoryEnd = containerEnd + 27;
			int hotbarEnd = inventoryEnd + 9;
			if (index >= inventoryEnd && index < hotbarEnd) {
				if (!moveItemStackTo(itemStack, containerEnd, inventoryEnd, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < inventoryEnd) {
				if (!moveItemStackTo(itemStack, inventoryEnd, hotbarEnd, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!moveItemStackTo(itemStack, inventoryEnd, inventoryEnd, false)) {
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
				&& isWithinTouchRange(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		container.stopOpen(player);
		if (closedByDistance) {
			PlayerUtils.showClientCenteredMessage(player,
					TextUtils.translate(Lang.TEXT_TARGET_ENTITY_TOO_FAR).withStyle(ChatFormatting.YELLOW));
		}
	}

	public Player getPlayer() {
		return inventory.player;
	}

	private boolean isWithinTouchRange(Player player) {
		boolean good = PlayerUtils.isWithinInteractionRange(player, entity, 4.0);
		if (!good) {
			closedByDistance = true;
		}
		return good;
	}

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
		public void setChanged() {}

		@Override
		public boolean stillValid(Player player) {
			return entity.isAlive() && PlayerUtils.isWithinInteractionRange(player, entity, 4.0);
		}
	}

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
			return PlayerUtils.isCreative(getPlayer()) && entity.isEquippableInSlot(itemStack, equipmentSlot);
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
			return true;
		}

		@Override
		public Identifier getNoItemIcon() {
			return emptyIcon;
		}
	}
}

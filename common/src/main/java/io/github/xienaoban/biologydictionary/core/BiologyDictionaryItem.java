package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A vanilla writable book marked with custom data so worlds remain readable without this mod.
 */
public final class BiologyDictionaryItem {
	public static final String ID = BiologyDictionary.MOD_ID;

	public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = ResourceKey.create(
			Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities"));

	@PlatformEntry
	public static final CreativeTabEntry BIOLOGY_DICTIONARY_BOOK_CREATIVE_TAB_ENTRY = new CreativeTabEntry(
			TOOLS_AND_UTILITIES, BiologyDictionaryItem::createBook);

	private static final CompoundTag ID_NBT = initIdNbt();

	private BiologyDictionaryItem() {}

	public static ItemStack createBook() {
		return createWritableBook();
	}

	public static boolean isBook(ItemStack stack) {
		if (stack == null || !stack.is(Items.WRITABLE_BOOK)) {
			return false;
		}
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		return customData != null && customData.copyTag().contains(ID);
	}

	public static void addToWanderingTraderTrades(WanderingTrader entity) {
		final int maxTicks = 2 * 24 * 60 * 60 * 20;
		int randomTicks = entity.getRandom().nextInt(maxTicks + (maxTicks >> 2));
		int currentTicks = (int) Math.min(EntityUtils.getLevel(entity).getGameTime(), maxTicks);
		if (randomTicks < currentTicks) {
			return;
		}

		final int cost = 64;
		final int maxUses = 3;
		final int villagerXp = 0;
		final float priceMultiplier = 0.05F;
		MerchantOffers offers = entity.getOffers();
		MerchantOffer offer = new MerchantOffer(new ItemCost(Items.EMERALD, cost), createBook(), maxUses, villagerXp, priceMultiplier);
		offers.add(offer);
	}

	private static ItemStack createWritableBook() {
		ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);

		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(ID_NBT));
		stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("biologydictionary:handbook"), List.of()));
		stack.set(DataComponents.ITEM_NAME, TextUtils.translate(Lang.BIOLOGY_DICTIONARY_TITLE).withStyle(
				Style.EMPTY.withColor(TextColor.parseColor("aqua").getOrThrow())
						.withBold(true).withItalic(false)
		));
		stack.set(DataComponents.LORE, ItemLore.EMPTY.withLineAdded(
				TextUtils.translate(Lang.BIOLOGY_DICTIONARY_DESCRIPTION).withStyle(
						Style.EMPTY.withColor(TextColor.parseColor("dark_aqua").getOrThrow())
								.withBold(false).withItalic(false)
				)
		));
		stack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(
				new Filterable<>(createWritablePageString(), Optional.empty())
		)));
		return stack;
	}

	private static CompoundTag initIdNbt() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString(ID, DevUtils.getModVersion(BiologyDictionary.MOD_ID));
		return nbt;
	}

	private static String createWritablePageString() {
		return """
				\u00a7l%s\u00a72\u00a7l%s
				
				\u00a7r\u00a70%s
				
				Modrinth: \u00a79\u00a7n%s
				
				CurseForge: \u00a79\u00a7n%s
				
				GitHub: \u00a79\u00a7n%s
				"""
				.formatted(
						trans(Lang.TEXT_MOD_NAME_IS),
						trans(Lang.BIOLOGY_DICTIONARY),
						trans(Lang.TEXT_MOD_NOT_INSTALLED),
						BiologyDictionary.MODRINTH_PAGE,
						BiologyDictionary.CURSEFORGE_PAGE,
						BiologyDictionary.GITHUB_PAGE);
	}

	private static String trans(String translateKey) {
		return TextUtils.translate(translateKey).getString();
	}

	public record CreativeTabEntry(ResourceKey<CreativeModeTab> tabKey, Supplier<ItemStack> stack) {
	}
}

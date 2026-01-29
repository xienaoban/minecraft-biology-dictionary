package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.server.ItemRegistry;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.mixin.CustomDataIMixin;
import io.github.xienaoban.biologydictionary.mixin.MinecraftMixin;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

/**
 * If the mod is installed correctly, a biology dictionary screen will be opened when the player right-clicks the book.
 * But if the mod is not installed, a vanilla book screen will be opened which displays the download address.
 * <p>
 * I didn't choose to define a new book item, instead I just made a book with custom NBT to ensure a good compatibility.
 * And I implemented the opening of the book in the mixin.
 *
 * @see MinecraftMixin#useBiologyDictionaryScreen(CallbackInfo)
 */
public final class BiologyDictionaryItem {
    // Any writable book with this nbt key will be recognized as a biology dictionary.
    public static final String ID = BiologyDictionary.MOD_ID;

    private static final CompoundTag ID_NBT = initIdNbt();

    public static void init() {
        ItemRegistry.register(CreativeModeTabs.TOOLS_AND_UTILITIES, createBook());
    }

    /**
     * The probability of the trade offer decreases as the in-game time progresses.
     * After 2 real-world days have passed, or approximately 10 spawns of wandering
     * traders, the probability will stabilize at 20%.
     * <p>
     * On average, a wandering trader will spawn approximately every 14.325 in-game
     * days (286.5 minutes).
     * <p>
     * real_world_days = 2
     * total_ticks = 2 * 24 * 60 * 60 * 20 = 3456000
     * ticks_per_game_day = 20 * 60 * 20 = 24000
     * game_days = 3456000 / 24000 = 144
     * spawn_count = 144 / 14.325 = 10
     *
     * @see net.minecraft.world.entity.npc.villager.VillagerTrades
     */
    public static void addToWanderingTraderTrades(WanderingTrader entity) {
        if (!ConfigsManager.getServer().isBookItemObtainableFromWanderingTrader()) {
            return;
        }

        final int maxTicks = 2 * 24 * 60 * 60 * 20;
        int r = entity.getRandom().nextInt(maxTicks + (maxTicks >> 2));
        int t = (int) Math.min(EntityUtils.getLevel(entity).getDayTime(), maxTicks);
        if (r < t) { return; }

        final int cost = 64;
        final int maxUses = 3;
        final int villagerXp = 0;
        final float priceMultiplier = 0.05F;
        MerchantOffers offers = entity.getOffers();
        MerchantOffer offer = new MerchantOffer(new ItemCost(Items.EMERALD, cost), createBook(), maxUses, villagerXp, priceMultiplier);
        offers.add(offer);
    }

    public static ItemStack createBook() {
        return createWritableBook();
    }

    @SuppressWarnings("all")
    public static boolean isBook(ItemStack stack) {
        if (stack == null || !stack.is(Items.WRITABLE_BOOK)) return false;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && ((CustomDataIMixin) (Object) cd).getTag().contains(ID);
    }

    private static ItemStack createWritableBook() {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(ID_NBT));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of("biologydictionary:handbook"), List.of()));
        stack.set(DataComponents.ITEM_NAME, Component.translatable(Lang.BIOLOGY_DICTIONARY_TITLE).withStyle(
                Style.EMPTY.withColor(TextColor.parseColor("aqua").getOrThrow())
                        .withBold(true).withItalic(false)
        ));
        stack.set(DataComponents.LORE, ItemLore.EMPTY.withLineAdded(
                Component.translatable(Lang.BIOLOGY_DICTIONARY_DESCRIPTION).withStyle(
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
                §l%s§2§l%s
                
                §r§0%s
                
                Modrinth: §9§n%s
                """
                .formatted(
                        trans(Lang.TEXT_MOD_NAME_IS),
                        trans(Lang.BIOLOGY_DICTIONARY),
                        trans(Lang.TEXT_MOD_NOT_INSTALLED),
                        BiologyDictionary.MODRINTH_PAGE);
    }

    /**
     * If the player doesn't have the mod installed, then the translation files will not be in the client either.
     *
     * @return translated string of the current language
     */
    private static String trans(String translateKey) {
        return Component.translatable(translateKey).getString();
    }
}

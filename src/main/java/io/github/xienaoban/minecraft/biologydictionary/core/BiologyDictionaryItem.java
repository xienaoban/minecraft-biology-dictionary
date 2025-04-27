package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import io.github.xienaoban.minecraft.biologydictionary.common.mixin.MinecraftMixin;
import io.github.xienaoban.minecraft.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.minecraft.biologydictionary.Lang;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
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
public class BiologyDictionaryItem {
    // Any writable book with this nbt key will be recognized as a biology dictionary.
    public static final String ID = BiologyDictionary.MOD_ID;

    private static final CompoundTag ID_TAG = initIdTag();

    public static ItemStack createBook() {
        return createWritableBook();
    }

    public static boolean isBook(ItemStack stack) {
        if (stack == null || !stack.is(Items.WRITABLE_BOOK)) return false;
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.contains(ID);
    }

    private static ItemStack createWritableBook() {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(ID_TAG));
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

    private static CompoundTag initIdTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString(ID, DevUtils.getModVersion(BiologyDictionary.MOD_ID));
        return tag;
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

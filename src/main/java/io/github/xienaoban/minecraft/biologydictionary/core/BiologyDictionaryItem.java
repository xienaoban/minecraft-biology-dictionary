package io.github.xienaoban.minecraft.biologydictionary.core;

import io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary;
import io.github.xienaoban.minecraft.biologydictionary.platform.access.DevApi;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

/**
 * If the mod is installed correctly, a biology dictionary screen will be opened when the player right-clicks the book.
 * But if the mod is not installed, a vanilla book screen will be opened which displays the download address.
 * <p>
 * I didn't choose to define a new book item, instead I just made a book with custom NBT to ensure a good compatibility.
 * And I implemented the opening of the book in the mixin.
 * @see
 */
public class BiologyDictionaryItem {
    // Any writable book with this nbt key will be recognized as a biology dictionary.
    public static final String ID = BiologyDictionary.MOD_KEY;

    public static ItemStack createBook() {
        return createWritableBook();
        // return createWrittenPages();
    }

    private static ItemStack createWritableBook() {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        CompoundTag nbt = new CompoundTag();
        addCoreNbt(nbt);
        nbt.put(WrittenBookItem.TAG_PAGES, createWritablePages());
        stack.setTag(nbt);
        return stack;
    }

    private static void addCoreNbt(CompoundTag nbt) {
        nbt.putString(ID, createVersion());
        nbt.put("display", createDisplay());
        nbt.putInt("CustomModelData", 14489768);
    }

    private static String createVersion() {
        return DevApi.getModVersion(BiologyDictionary.MOD_ID);
    }

    private static Tag createDisplay() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Name", "{\"translate\": \"" + TranslationKeys.BIOLOGY_DICTIONARY_TITLE + "\", \"color\": \"aqua\", \"bold\": true, \"italic\": false}");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf("{\"translate\": \"" + TranslationKeys.BIOLOGY_DICTIONARY_DESCRIPTION + "\", \"color\": \"dark_aqua\", \"italic\": false}"));
        nbt.put("Lore", lore);
        return nbt;
    }

    @SuppressWarnings("unused")
    private static ListTag createWritablePages() {
        String page0 = """
                §l%s§2§l%s
                
                §r§0%s
                
                Modrinth: §9§n%s
                """
                .formatted(
                        trans(TranslationKeys.TEXT_MOD_NAME_IS),
                        trans(TranslationKeys.BIOLOGY_DICTIONARY),
                        trans(TranslationKeys.TEXT_MOD_NOT_INSTALLED),
                        BiologyDictionary.MODRINTH_PAGE);
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(page0));
        return pages;
    }

    @SuppressWarnings("unused")
    private static ListTag createWrittenPages() {
        String page0Json = """
                [
                    {
                        "text": "%s",
                        "color": "black",
                        "bold": true
                    },
                    {
                        "text": "%s",
                        "color": "dark_green",
                        "bold": true
                    },
                    "\\n\\n",
                    {
                        "text": "%s",
                        "color": "black",
                        "bold": false
                    },
                    "\\n\\n",
                    {
                        "text": "[ Github ]",
                        "color": "blue",
                        "bold": false,
                        "underlined": true,
                        "clickEvent": {
                            "action": "open_url",
                            "value": "%s"
                        },
                        "hoverEvent": {
                            "action": "show_text",
                            "contents": {
                                "text": "%s"
                            }
                        }
                    }
                ]
                """
                .formatted(trans(TranslationKeys.TEXT_MOD_NAME_IS), trans(TranslationKeys.BIOLOGY_DICTIONARY), trans(TranslationKeys.TEXT_MOD_NOT_INSTALLED), BiologyDictionary.MODRINTH_PAGE, trans(TranslationKeys.TEXT_CLICK_ME_TO_DOWNLOAD));
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(page0Json));
        return pages;
    }

    /**
     * If the player doesn't have the mod installed, then the translation files will not be in the client either.
     * @return translated string of the current language
     */
    private static String trans(String translateKey) {
        return Component.translatable(translateKey).getString();
    }
}

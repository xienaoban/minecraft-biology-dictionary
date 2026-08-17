package io.github.xienaoban.biologydictionary.compat.jei;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IIngredientAliasRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Biology Dictionary book is a vanilla writable book with custom components.
 * JEI builds its item list from creative tabs and deduplicates by item uid, which
 * does not include components, so without a subtype interpreter the book would be
 * hidden behind the plain writable book.
 * <p>
 * Discovered via the {@code @JeiPlugin} annotation on NeoForge and the
 * {@code jei_mod_plugin} entrypoint on Fabric.
 *
 * @see mezz.jei.library.plugins.vanilla.ingredients.ItemStackListFactory
 * @see ISubtypeRegistration
 */
@ClientOnly
@JeiPlugin
public class BiologyDictionaryJeiPlugin implements IModPlugin {
    /**
     * An NBT-based variant of a vanilla item displayed in JEI.
     * The subtype key must be unique among the variants of the same base item,
     * so that several variants on one base item do not deduplicate each other.
     * Search aliases work regardless of the client language.
     */
    private record NbtItemVariant(String subtypeKey, Supplier<ItemStack> stackFactory,
                                  Predicate<ItemStack> tester, List<String> aliases) {}

    /**
     * Register new NBT-based items here when they are added to the mod.
     */
    private static final Map<Item, List<NbtItemVariant>> NBT_ITEM_VARIANTS = Map.of(
            Items.WRITABLE_BOOK, List.of(new NbtItemVariant(
                    BiologyDictionary.MOD_ID + ":book",
                    BiologyDictionaryItem::createBook,
                    BiologyDictionaryItem::isBook,
                    List.of("生物辞典", "Biology Dictionary", "shengwucidian")))
    );

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(BiologyDictionary.MOD_ID, "jei");
    }

    /**
     * NBT-based variants are distinct subtypes of their base items,
     * so they get their own uids instead of being deduplicated into the plain ones.
     */
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        NBT_ITEM_VARIANTS.forEach((item, variants) ->
                registration.registerSubtypeInterpreter(item, (stack, context) -> {
                    for (NbtItemVariant variant : variants) {
                        if (variant.tester().test(stack)) {
                            return variant.subtypeKey();
                        }
                    }
                    return null;
                }));
    }

    /**
     * The variants are already in the creative menu; add them explicitly as a fallback,
     * e.g. for modpacks that remove or hide the creative tab entries.
     */
    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> stacks = NBT_ITEM_VARIANTS.values().stream()
                .flatMap(List::stream)
                .map(variant -> variant.stackFactory().get())
                .toList();
        registration.addExtraItemStacks(stacks);
    }

    @Override
    public void registerIngredientAliases(IIngredientAliasRegistration registration) {
        NBT_ITEM_VARIANTS.values().stream().flatMap(List::stream).forEach(variant -> {
            ItemStack stack = variant.stackFactory().get();
            for (String alias : variant.aliases()) {
                registration.addAlias(stack, alias);
            }
        });
    }
}

package io.github.xienaoban.biologydictionary.compat.akashictome;

import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.core.BiologyDictionaryItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

/**
 * Reads the books stored in an Akashic Tome stack.
 *
 * <p>Akashic Tome stores the books in a dedicated data component. The component type belongs to
 * Akashic Tome, so it is looked up by id and its public getter is invoked through a method handle.
 * This class is only initialized after Akashic Tome is detected, and the method handle is resolved
 * once by the static initializer; if that fails, the compatibility is disabled for the session.
 */
public final class AkashicTomeCompat {
    private static final ResourceLocation TOOL_CONTENT_ID = ResourceLocation.fromNamespaceAndPath("akashictome", "tool_content");
    private static final MethodHandle GET_ITEMS = findGetItems();
    private static final boolean ENABLED = GET_ITEMS != null;
    private static DataComponentType<?> toolContentType;
    private static boolean missingTypeReported;
    private static boolean readFailed;

    private AkashicTomeCompat() {}

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static boolean containsBook(ItemStack stack) {
        if (!ENABLED || stack == null || stack.isEmpty()) { return false; }
        DataComponentType<?> type = getToolContentType();
        if (type == null) { return false; }
        Object component = stack.get(type);
        if (component == null) { return false; }
        try {
            Object items = GET_ITEMS.invoke(component);
            if (items instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof ItemStack book && BiologyDictionaryItem.isBook(book)) { return true; }
                }
            }
        } catch (Throwable e) {
            if (!readFailed) {
                BiologyDictionary.LOGGER.error("Failed to read Akashic Tome contents.", e);
                readFailed = true;
            }
        }
        return false;
    }

    private static MethodHandle findGetItems() {
        try {
            Class<?> componentClass = Class.forName("vazkii.akashictome.data_components.ToolContentComponent",
                    false, AkashicTomeCompat.class.getClassLoader());
            return MethodHandles.publicLookup().findVirtual(componentClass, "getItems", MethodType.methodType(List.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            BiologyDictionary.LOGGER.error("Failed to initialize Akashic Tome compatibility; book-in-tome detection is disabled.", e);
            return null;
        }
    }

    private static DataComponentType<?> getToolContentType() {
        if (toolContentType == null) {
            toolContentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(TOOL_CONTENT_ID);
            if (toolContentType == null && !missingTypeReported) {
                BiologyDictionary.LOGGER.error("Akashic Tome tool content component is missing; book-in-tome detection is unavailable.");
                missingTypeReported = true;
            }
        }
        return toolContentType;
    }
}

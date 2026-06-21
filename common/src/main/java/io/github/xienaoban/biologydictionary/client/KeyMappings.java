package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyMappings {
    private static final String OPEN_HANDBOOK_KEY = "key.biologydictionary.open_handbook";
    private static final String DEBUG_KEY = "key.biologydictionary.debug";

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(BiologyDictionary.MOD_ID, BiologyDictionary.MOD_ID));

    @PlatformEntry
    public static final KeyMapping OPEN_HANDBOOK = new KeyMapping(
            OPEN_HANDBOOK_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY);

    @PlatformEntry
    public static final KeyMapping DEBUG = new KeyMapping(
            DEBUG_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            CATEGORY);

    private KeyMappings() {}
}

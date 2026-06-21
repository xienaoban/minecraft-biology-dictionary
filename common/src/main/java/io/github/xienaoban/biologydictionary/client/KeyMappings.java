package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyMappings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(BiologyDictionary.MOD_ID, BiologyDictionary.MOD_ID));

    @PlatformEntry
    public static final KeyMapping OPEN_HANDBOOK = new KeyMapping(
            Lang.KEY_OPEN_HANDBOOK,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            CATEGORY);

    public static final KeyMapping DEBUG = new KeyMapping(
            Lang.KEY_DEBUG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            CATEGORY);

    private KeyMappings() {}
}

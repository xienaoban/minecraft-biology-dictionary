package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.PlatformEntry;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@ClientOnly
public final class KeyMappings {
    @PlatformEntry
    public static final KeyMapping OPEN_HANDBOOK = new KeyMapping(
            Lang.KEY_OPEN_HANDBOOK,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            Lang.KEY_CATEGORY);

    public static final KeyMapping STEAL_INVENTORY = new KeyMapping(
            Lang.KEY_STEAL_INVENTORY,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            Lang.KEY_CATEGORY);

    public static final KeyMapping DEBUG = new KeyMapping(
            Lang.KEY_DEBUG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT,
            Lang.KEY_CATEGORY);

    private KeyMappings() {}
}

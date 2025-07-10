package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.client.ClientEventRegistry;
import io.github.xienaoban.biologydictionary.common.client.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class KeyMappingManager {
    public static final KeyMapping OPEN_BIOLOGY_DICTIONARY_SCREEN = new KeyMapping(Lang.KEY_OPEN_HANDBOOK, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, Lang.KEY_CATEGORY);
    public static final KeyMapping TOGGLE_DEBUG = new KeyMapping(Lang.KEY_DEBUG, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, Lang.KEY_CATEGORY);

    public static void init() {
        KeyMappingRegistry.registerKeyMapping(OPEN_BIOLOGY_DICTIONARY_SCREEN);
        KeyMappingRegistry.registerKeyMapping(TOGGLE_DEBUG);
        ClientEventRegistry.registerEndTick(minecraft -> {
            while (OPEN_BIOLOGY_DICTIONARY_SCREEN.consumeClick()) {
                if (minecraft.player != null) {
                    BiologyDictionaryEvent.openBookScreen(minecraft);
                }
            }
        });
    }
}

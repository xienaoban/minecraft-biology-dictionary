package io.github.xienaoban.minecraft.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.minecraft.biologydictionary.gui.screen.AbstractBiologyDictionaryScreen;
import io.github.xienaoban.minecraft.biologydictionary.platform.client.ClientEventRegistry;
import io.github.xienaoban.minecraft.biologydictionary.platform.client.KeyMappingRegistry;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class KeyMappingManager {
    public static final KeyMapping OPEN_BIOLOGY_DICTIONARY_SCREEN = new KeyMapping(TranslationKeys.KEY_OPEN_HANDBOOK, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, TranslationKeys.KEY_CATEGORY);
    public static final KeyMapping TOGGLE_DEBUG = new KeyMapping(TranslationKeys.KEY_DEBUG, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, TranslationKeys.KEY_CATEGORY);

    public static void init() {
        KeyMappingRegistry.registerKeyMapping(OPEN_BIOLOGY_DICTIONARY_SCREEN);
        KeyMappingRegistry.registerKeyMapping(TOGGLE_DEBUG);
        ClientEventRegistry.registerEndTick(client -> {
            while (OPEN_BIOLOGY_DICTIONARY_SCREEN.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new AbstractBiologyDictionaryScreen());
                }
            }
        });
    }
}

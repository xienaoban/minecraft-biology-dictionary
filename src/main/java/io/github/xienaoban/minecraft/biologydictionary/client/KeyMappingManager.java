package io.github.xienaoban.minecraft.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.minecraft.biologydictionary.platform.client.ClientEventRegistry;
import io.github.xienaoban.minecraft.biologydictionary.platform.client.KeyMappingRegistry;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKey;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class KeyMappingManager {
    public static final KeyMapping KM_BIOLOGY_DICTIONARY_SCREEN = new KeyMapping(TranslationKey.KEY_OPEN_HANDBOOK, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, TranslationKey.KEY_CATEGORY);

    public static void init() {
        KeyMappingRegistry.registerKeyMapping(KM_BIOLOGY_DICTIONARY_SCREEN);
        ClientEventRegistry.registerEndTick(client -> {
            while (KM_BIOLOGY_DICTIONARY_SCREEN.consumeClick()) {
                if (client.player != null) {
                    client.player.displayClientMessage(Component.translatable(TranslationKey.THANKS), false);
                }
            }
        });
    }
}

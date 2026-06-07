package io.github.xienaoban.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.client.ClientEventRegistry;
import io.github.xienaoban.biologydictionary.platform.client.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

@ClientOnly
public final class KeyMappingManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Lang.BIOLOGY_DICTIONARY, Lang.BIOLOGY_DICTIONARY));

    public static final KeyMapping OPEN_BIOLOGY_DICTIONARY_SCREEN = new KeyMapping(Lang.KEY_OPEN_HANDBOOK, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, CATEGORY);
    public static final KeyMapping TOGGLE_DEBUG = new KeyMapping(Lang.KEY_DEBUG, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, CATEGORY);

    public static void init() {
        KeyMappingRegistry.registerKeyMapping(OPEN_BIOLOGY_DICTIONARY_SCREEN);
        ClientEventRegistry.registerEndTick(client -> {
            while (OPEN_BIOLOGY_DICTIONARY_SCREEN.consumeClick()) {
                if (client.player != null) {
                    BiologyDictionaryEvent.openBookScreen(client);
                }
            }
        });
    }
}

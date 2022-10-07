package xienaoban.minecraft.biologydictionary.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import xienaoban.minecraft.biologydictionary.util.Keys;

@Environment(EnvType.CLIENT)
public interface KeyMappingManager {
    KeyMapping KEY_BOLE_SCREEN = new KeyMapping(Keys.KEY_OPEN_MOD_GUI, InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT, Keys.KEY_MOD_CATEGORY);

    @ExpectPlatform
    static void init() {
        // 1. Register the KeyMapping(s) to the mod loader.
        // 2. Register when to call KeyMapping(s).
        throw new AssertionError();
    }

    static void onClientTickEnd(Minecraft client) {
        while (KeyMappingManager.KEY_BOLE_SCREEN.consumeClick()) {
            ClientEvents.onOpenBiologyDictionary(client);
        }
    }
}

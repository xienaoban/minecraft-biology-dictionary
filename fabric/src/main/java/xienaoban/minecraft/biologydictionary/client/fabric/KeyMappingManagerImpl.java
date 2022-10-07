package xienaoban.minecraft.biologydictionary.client.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import xienaoban.minecraft.biologydictionary.client.KeyMappingManager;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class KeyMappingManagerImpl {
    public static void init() {
        if (KeyBindingHelper.registerKeyBinding(KeyMappingManager.KEY_BOLE_SCREEN) == null) throw new AssertionError();
        ClientTickEvents.END_CLIENT_TICK.register(KeyMappingManager::onClientTickEnd);
    }
}

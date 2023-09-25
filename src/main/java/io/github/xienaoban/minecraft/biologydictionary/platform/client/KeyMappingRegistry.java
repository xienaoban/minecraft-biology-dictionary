package io.github.xienaoban.minecraft.biologydictionary.platform.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public final class KeyMappingRegistry {
    public static void registerKeyMapping(KeyMapping mapping) {
        KeyBindingHelper.registerKeyBinding(mapping);
    }
}

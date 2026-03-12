package io.github.xienaoban.biologydictionary.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.xienaoban.biologydictionary.config.ClothConfigScreenProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * ModMenu integration for Cloth Config screen.
 */
@Environment(EnvType.CLIENT)
public final class ModMenuConfigScreenProvider implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClothConfigScreenProvider::provideScreen;
    }
}

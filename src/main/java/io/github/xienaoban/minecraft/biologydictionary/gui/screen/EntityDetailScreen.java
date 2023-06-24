package io.github.xienaoban.minecraft.biologydictionary.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EntityDetailScreen extends AbstractBiologyDictionaryScreen {
    private final Entity entity;

    public EntityDetailScreen(Entity entity) {
        this.entity = entity;
    }
}

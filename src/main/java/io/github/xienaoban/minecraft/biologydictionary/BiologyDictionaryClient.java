package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.client.EntityWidgetManager;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryClient {
    public static final BiologyDictionaryClient BDC = new BiologyDictionaryClient();

    private Entity hitEntity;
    private BlockPos hitBlock;

    private BiologyDictionaryClient() {
        hitEntity = null;
        hitBlock = null;

        KeyMappingManager.init();
        FirstPersonShoulderEntityRenderer.init();
        EntityWidgetManager.init();
        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public Entity getHitEntity() {
        return hitEntity;
    }

    public void setHitEntity(Entity hitEntity) {
        this.hitEntity = hitEntity;
    }

    public BlockPos getHitBlock() {
        return hitBlock;
    }

    public void setHitBlock(BlockPos hitBlock) {
        this.hitBlock = hitBlock;
    }
}

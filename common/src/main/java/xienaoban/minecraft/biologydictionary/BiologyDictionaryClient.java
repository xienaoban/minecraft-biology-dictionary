package xienaoban.minecraft.biologydictionary;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import xienaoban.minecraft.biologydictionary.client.KeyMappingManager;

@Environment(EnvType.CLIENT)
public class BiologyDictionaryClient {
    private static class BiologyDictionaryClientHolder {
        private static final BiologyDictionaryClient INSTANCE = new BiologyDictionaryClient();
    }

    public static BiologyDictionaryClient get() {
        return BiologyDictionaryClientHolder.INSTANCE;
    }

    private boolean isScreenOpen;
    private Entity hitEntity;
    private BlockPos hitBlock;

    private BiologyDictionaryClient() {
        KeyMappingManager.init();
    }

    public boolean isScreenOpen() {
        return isScreenOpen;
    }

    public void setScreenOpen(boolean screenOpen) {
        isScreenOpen = screenOpen;
    }

    public Entity getHitEntity() {
        Entity entity = hitEntity;
        hitEntity = null;
        return entity;
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

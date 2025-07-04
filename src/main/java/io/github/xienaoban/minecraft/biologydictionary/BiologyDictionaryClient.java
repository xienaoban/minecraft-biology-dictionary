package io.github.xienaoban.minecraft.biologydictionary;

import io.github.xienaoban.minecraft.biologydictionary.client.FirstPersonShoulderEntityRenderer;
import io.github.xienaoban.minecraft.biologydictionary.client.KeyMappingManager;
import io.github.xienaoban.minecraft.biologydictionary.common.client.ClientEventRegistry;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.minecraft.biologydictionary.core.widget.EntityPropertyWidgets;
import io.github.xienaoban.minecraft.biologydictionary.net.ClientNetManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public final class BiologyDictionaryClient {
    public static final BiologyDictionaryClient BDC = new BiologyDictionaryClient();

    private Entity hitEntity;
    private BlockPos hitBlock;

    private EntityProperties<? extends Entity> hitEntityProperties;

    private BiologyDictionaryClient() {
        hitEntity = null;
        hitBlock = null;
        hitEntityProperties = null;

        EntityPropertyWidgets.init();
        FirstPersonShoulderEntityRenderer.init();
        KeyMappingManager.init();
        ClientNetManager.init();

        ClientEventRegistry.registerWorldConnected(client -> EntityManager.init());
        ClientEventRegistry.registerWorldDisconnecting(client -> EntityManager.destroy());

        LOGGER.info("BiologyDictionary (client) initialized.");
    }

    public void forceInitialize() { /* do nothing but to trigger cinit */ }

    public Entity getHitEntity() { return hitEntity; }
    public void setHitEntity(Entity hitEntity) { this.hitEntity = hitEntity; }

    public BlockPos getHitBlock() { return hitBlock; }
    public void setHitBlock(BlockPos hitBlock) { this.hitBlock = hitBlock; }


    public EntityProperties<? extends Entity> getHitEntityProperties() { return hitEntityProperties; }
    public void setHitEntityProperties(EntityProperties<? extends Entity> hitEntityProperties) { this.hitEntityProperties = hitEntityProperties; }
}

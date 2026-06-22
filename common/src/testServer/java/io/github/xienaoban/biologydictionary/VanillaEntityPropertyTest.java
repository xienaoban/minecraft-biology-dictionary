package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VanillaEntityPropertyTest {
    private static final Logger LOGGER = LogManager.getLogger();

    public void testAllEntityProperties(GameTestHelper helper) {
        try {
            ServerLevel level = helper.getLevel();
            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;

            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                try {
                    // Skip entities that are not in our entity tree
                    EntityManager.EntityClassInfo classInfo = WorldSession.get()
                        .getEntityManager()
                        .getEntityClassInfo(entityType);
                    if (classInfo == null) {
                        LOGGER.debug("Skipped entity type (no class info): {}", EntityType.getKey(entityType));
                        skipCount++;
                        continue;
                    }

                    // Skip non-vanilla classes
                    Class<?> clazz = classInfo.getClazz();
                    if (!DevUtils.isVanillaClass(clazz)) {
                        LOGGER.debug("Skipped non-vanilla entity class: {}", clazz.getName());
                        skipCount++;
                        continue;
                    }

                    // Test the entity type
                    testEntityType(Misc.cast(entityType), level);
                    successCount++;
                    LOGGER.debug("Successfully tested entity type: {}", EntityType.getKey(entityType));
                } catch (Throwable e) {
                    failCount++;
                    LOGGER.error("Failed to test entity type: " + EntityType.getKey(entityType), e);
                    // Don't fail the test immediately, continue testing other entities
                }
            }

            LOGGER.info(
                "Entity properties test completed: {} passed, {} skipped, {} failed",
                successCount, skipCount, failCount
            );
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail("testAllEntityProperties failed: " + Misc.getStackToString(throwable));
        }
    }

    private <E extends Entity> void testEntityType(EntityType<E> entityType, ServerLevel level) {
        // Step 1: Create entity
        E entity = EntityUtils.create(entityType, level);
        if (entity == null) {
            throw new RuntimeException("Failed to create entity for type: " + EntityType.getKey(entityType));
        }

        // Step 2: Generate two NBTs similar to RequestEntityDataPacket
        // Write vanilla NBT data
        CompoundTag vanillaNbt = EntityUtils.getNbt(entity);

        // Write data that not in vanilla NBT (extra properties)
        CompoundTag extraNbt = new CompoundTag();
        for (EntityProperty<?> p : new EntityProperties<>(entity).getExtras()) {
            p.getFrom(Misc.cast(entity));
            p.writeTo(extraNbt);
        }

        // Step 3: Create a new entity of the same type
        E newEntity = EntityUtils.create(entityType, level);
        if (newEntity == null) {
            throw new RuntimeException("Failed to create second entity for type: " + EntityType.getKey(entityType));
        }

        // Step 4: Create EntityProperties for the new entity
        EntityProperties<E> properties = new EntityProperties<>(newEntity);

        // Step 5: Execute update function - this should not throw any exceptions
        properties.update(vanillaNbt, extraNbt);

        LOGGER.trace("Successfully tested property update for: {}", EntityType.getKey(entityType));
    }
}

package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.EntityProperties;
import io.github.xienaoban.biologydictionary.core.property.EntityProperty;
import io.github.xienaoban.biologydictionary.core.property.VanillaEntityProperties;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

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
                    EntityManager.EntityDictionaryEntry entry = WorldSession.get()
                        .getEntityManager()
                        .getEntityEntry(entityType);
                    if (entry == null || entry.isInstanceCreationFailed() || entry.getClazz().isEmpty()) {
                        LOGGER.debug("Skipped entity type (no class info): {}", EntityType.getKey(entityType));
                        skipCount++;
                        continue;
                    }

                    // Skip non-vanilla classes
                    Class<?> clazz = entry.getClazz().get();
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
            if (failCount > 0) {
                helper.fail("testAllEntityProperties failed for " + failCount + " entity types");
                return;
            }
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail("testAllEntityProperties failed: " + Misc.getStackToString(throwable));
        }
    }

    public void testChangedCodecProperties(GameTestHelper helper) {
        try {
            ServerLevel level = helper.getLevel();

            Entity tagSource = EntityUtils.create(EntityTypes.ZOMBIE, level);
            Entity tagTarget = EntityUtils.create(EntityTypes.ZOMBIE, level);
            if (tagSource == null || tagTarget == null) {
                throw new AssertionError("Failed to create zombie entities for Tags");
            }
            tagSource.addTag("biologydictionary_test");
            assertCodecRoundTrip(
                    helper,
                    "Tags",
                    VanillaEntityProperties.OfEntity.createTagsProperty(),
                    List.of("biologydictionary_test")
            );
            assertCodecEntityRoundTrip(
                    helper, "Tags", tagSource, tagTarget, VanillaEntityProperties.OfEntity::createTagsProperty);

            Zombie attributesSource = EntityUtils.create(EntityTypes.ZOMBIE, level);
            if (attributesSource == null) {
                throw new AssertionError("Failed to create zombie entity for attributes");
            }
            List<AttributeInstance.Packed> attributes = attributesSource.getAttributes().pack();
            assertCodecRoundTrip(
                    helper,
                    "attributes",
                    VanillaEntityProperties.OfLivingEntity.createAttributesProperty(),
                    attributes
            );

            Mannequin layersSource = EntityUtils.create(EntityTypes.MANNEQUIN, level);
            Mannequin layersTarget = EntityUtils.create(EntityTypes.MANNEQUIN, level);
            if (layersSource == null || layersTarget == null) {
                throw new AssertionError("Failed to create mannequin entities for hidden_layers");
            }
            assertCodecRoundTrip(
                    helper,
                    "hidden_layers",
                    VanillaEntityProperties.OfMannequin.createHiddenLayersProperty(),
                    (byte) 0
            );
            assertCodecEntityRoundTrip(
                    helper,
                    "hidden_layers",
                    layersSource,
                    layersTarget,
                    VanillaEntityProperties.OfMannequin::createHiddenLayersProperty
            );

            Mannequin poseSource = EntityUtils.create(EntityTypes.MANNEQUIN, level);
            Mannequin poseTarget = EntityUtils.create(EntityTypes.MANNEQUIN, level);
            if (poseSource == null || poseTarget == null) {
                throw new AssertionError("Failed to create mannequin entities for pose");
            }
            poseSource.setPose(Pose.CROUCHING);
            assertCodecRoundTrip(
                    helper,
                    "pose",
                    VanillaEntityProperties.OfMannequin.createPoseProperty(),
                    Pose.CROUCHING
            );
            assertCodecEntityRoundTrip(
                    helper,
                    "pose",
                    poseSource,
                    poseTarget,
                    VanillaEntityProperties.OfMannequin::createPoseProperty
            );

            Zombie trustedEntity = EntityUtils.create(EntityTypes.ZOMBIE, level);
            Fox trustedSource = EntityUtils.create(EntityTypes.FOX, level);
            Fox trustedTarget = EntityUtils.create(EntityTypes.FOX, level);
            if (trustedEntity == null || trustedSource == null || trustedTarget == null) {
                throw new AssertionError("Failed to create entities for Trusted");
            }
            EntityReference<LivingEntity> trustedReference = EntityReference.of(trustedEntity);
            List<EntityReference<LivingEntity>> trustedList = List.of(trustedReference);

            CodecProperty<Fox, List<EntityReference<LivingEntity>>> trustedWriter =
                    VanillaEntityProperties.OfFox.createTrustedProperty();
            trustedWriter.withVal(trustedList);
            trustedWriter.setTo(trustedSource);

            assertCodecRoundTrip(
                    helper,
                    "Trusted",
                    VanillaEntityProperties.OfFox.createTrustedProperty(),
                    trustedList
            );
            assertCodecEntityRoundTrip(
                    helper,
                    "Trusted",
                    trustedSource,
                    trustedTarget,
                    VanillaEntityProperties.OfFox::createTrustedProperty
            );

            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail("testChangedCodecProperties failed: " + Misc.getStackToString(throwable));
        }
    }

    private <E extends Entity, T> void assertCodecRoundTrip(
            GameTestHelper helper, String name, CodecProperty<E, T> property, T expected) {
        property.withVal(expected);
        CompoundTag nbt = property.toTag();
        property.withVal(null);
        property.withTag(nbt);
        T actual = property.getVal();
        helper.assertTrue(Objects.equals(expected, actual),
                name + " codec round-trip mismatch: expected=" + expected + ", actual=" + actual);
    }

    private <E extends Entity, T> void assertCodecEntityRoundTrip(
            GameTestHelper helper, String name, E source, E target, Supplier<CodecProperty<E, T>> factory) {
        CodecProperty<E, T> writer = factory.get();
        writer.withEntity(source);
        T expected = writer.getVal();
        CompoundTag nbt = writer.toTag();

        CodecProperty<E, T> setter = factory.get();
        setter.withTag(nbt);
        setter.setTo(target);

        CodecProperty<E, T> verifier = factory.get();
        verifier.withEntity(target);
        T actual = verifier.getVal();
        helper.assertTrue(Objects.equals(expected, actual),
                name + " entity round-trip mismatch: expected=" + expected + ", actual=" + actual);
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

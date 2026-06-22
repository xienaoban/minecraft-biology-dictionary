package io.github.xienaoban.biologydictionary;

import com.mojang.authlib.GameProfile;
import io.github.xienaoban.biologydictionary.core.property.bundle.EntityVariantPropertyBundle;
import io.github.xienaoban.biologydictionary.core.skill.*;
import io.github.xienaoban.biologydictionary.core.skill.entity.*;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.GameType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class VanillaEntitySkillTest {
    private static final Logger LOGGER = LogManager.getLogger();

    // Functional interface for creating skill instances
    @FunctionalInterface
    public interface EntitySkillFactory<E extends Entity, S extends EntityTargetedSkill<?>> {
        S create(E entity, ServerPlayer player);

        // Default method to skip this skill if it cannot be created
        default boolean shouldSkip(E entity) {
            return false;
        }
    }

    // Two maps as requested
    private static final Map<Class<? extends Entity>, List<Class<? extends EntityTargetedSkill<?>>>> ENTITY_TO_SKILLS =
        new HashMap<>();
    private static final Map<Class<?>, List<EntitySkillFactory<?, ?>>> SKILL_TO_FACTORY = new HashMap<>();

    static {
        registerBuiltIn();
    }

    public static void registerBuiltIn() {
        // Register all skills from BiologySkills and create mappings
        // First, auto-generate entity -> skill_class mapping from BiologySkills
        generateEntityToSkillMapping();

        // Then, manually register skill_class -> factory mappings
        registerSkillFactories();
    }

    private static void generateEntityToSkillMapping() {
        // Use BiologySkills#registerBuiltIn to iterate through all registered skills
        BiologySkills.registerBuiltIn(new BiologySkills.Registrar() {
            @Override
            public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                // Skip GeneralSkills, only process EntityTargetedSkills
            }

            @Override
            public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass,
                    EntityTargetedSkill.Meta<T> meta) {
                try {
                    // Get the entity class from the skill's generic type parameter
                    Class<? extends Entity> entityClass = Misc.getClazzGeneric(
                        skillClass, EntityTargetedSkill.class, 0).asSubclass(Entity.class);

                    ENTITY_TO_SKILLS.computeIfAbsent(entityClass, k -> new ArrayList<>())
                        .add(skillClass);

                    LOGGER.debug("Auto-registered skill {} for entity class {}",
                        skillClass.getSimpleName(), entityClass.getSimpleName());
                } catch (Exception e) {
                    LOGGER.error("Failed to process skill: {}", skillClass.getName(), e);
                    throw new RuntimeException("Failed to generate entity-to-skill mapping", e);
                }
            }
        });
    }

    private static void registerSkillFactories() {
        // EntitySetInvulnerableSkill - default shouldSkip
        registerFactory(EntitySetInvulnerableSkill.class, (entity, player) -> new EntitySetInvulnerableSkill(true));
        registerFactory(EntitySetInvulnerableSkill.class, (entity, player) -> new EntitySetInvulnerableSkill(false));

        // EntitySetVariantSkill - custom shouldSkip
        registerFactory(EntitySetVariantSkill.class, new EntitySkillFactory<Entity, EntitySetVariantSkill>() {
            @Override
            public EntitySetVariantSkill create(Entity entity, ServerPlayer player) {
                String entityTypeId = EntityUtils.getEntityTypeIdName(entity);
                EntityVariantPropertyBundle.VariantHandler<Entity, Object> handler =
                        EntityVariantPropertyBundle.getHandlers(entity).get(0);
                Tag variantTag = handler.variantToNbt(entity, handler.getVariant(entity));
                return new EntitySetVariantSkill(entityTypeId, 0, variantTag);
            }

            @Override
            public boolean shouldSkip(Entity entity) {
                return EntityVariantPropertyBundle.getHandlers(entity).isEmpty();
            }
        });

        // EntitySetSoundSkill - default shouldSkip
        registerFactory(EntitySetSoundSkill.class, (entity, player) -> new EntitySetSoundSkill(true));
        registerFactory(EntitySetSoundSkill.class, (entity, player) -> new EntitySetSoundSkill(false));

        // EntitySetPortalCooldownSkill - default shouldSkip
        registerFactory(EntitySetPortalCooldownSkill.class,
                (entity, player) -> new EntitySetPortalCooldownSkill(10));

        // MobSetNoAiSkill - default shouldSkip
        registerFactory(MobSetNoAiSkill.class, (entity, player) -> new MobSetNoAiSkill(true));
        registerFactory(MobSetNoAiSkill.class, (entity, player) -> new MobSetNoAiSkill(false));

        // MobForcePersistentSkill - default shouldSkip
        registerFactory(MobForcePersistentSkill.class,
                (entity, player) -> new MobForcePersistentSkill(true));

        // LivingEntityStealInventorySkill - custom shouldSkip
        registerFactory(LivingEntityStealInventorySkill.class,
                (entity, player) -> new LivingEntityStealInventorySkill());

        // AgeableMobSetBreedingCooldownSkill - default shouldSkip
        registerFactory(AgeableMobSetBreedingCooldownSkill.class,
                (entity, player) -> new AgeableMobSetBreedingCooldownSkill(-250));

        // AgeableMobSetAgeLockedSkill - default shouldSkip
        registerFactory(AgeableMobSetAgeLockedSkill.class,
                (entity, player) -> new AgeableMobSetAgeLockedSkill(true));

        // TadpoleSetAgeLockedSkill - default shouldSkip
        registerFactory(TadpoleSetAgeLockedSkill.class,
                (entity, player) -> new TadpoleSetAgeLockedSkill(true));

        // SheepForceEatGrassSkill - default shouldSkip
        registerFactory(SheepForceEatGrassSkill.class,
                (entity, player) -> new SheepForceEatGrassSkill());

        // BeeClearHiveSkill - default shouldSkip
        registerFactory(BeeClearHiveSkill.class,
                (entity, player) -> new BeeClearHiveSkill());

        // VillagerForceRestockSkill - default shouldSkip
        registerFactory(VillagerForceRestockSkill.class,
                (entity, player) -> new VillagerForceRestockSkill(
                    88, GlobalPos.of(player.level().dimension(), player.getOnPos())
                ));

        // WanderingTraderRetainSkill - default shouldSkip
        registerFactory(WanderingTraderRetainSkill.class,
                (entity, player) -> new WanderingTraderRetainSkill());

        // EntityGiftPetSkill - default shouldSkip
        registerFactory(EntityGiftPetSkill.class, new EntitySkillFactory<Entity, EntityGiftPetSkill>() {
            @Override
            public EntityGiftPetSkill create(Entity entity, ServerPlayer player) {
                return new EntityGiftPetSkill(UUID.randomUUID());
            }
            @Override
            public boolean shouldSkip(Entity entity) {
                return !(entity instanceof OwnableEntity);
            }
        });
    }

    private static <E extends Entity, S extends EntityTargetedSkill<?>> void registerFactory(
        Class<S> skillClass, EntitySkillFactory<E, S> factory) {
        // Add factory directly with default shouldSkip (false)
        SKILL_TO_FACTORY.computeIfAbsent(skillClass, k -> new ArrayList<>()).add(factory);
    }

    public void testSkillNamingConvention(GameTestHelper helper) {
        try {
            BiologySkills.registerBuiltIn(new BiologySkills.Registrar() {
                @Override
                public <T extends GeneralSkill> void register(Class<T> skillClass, GeneralSkill.Meta<T> meta) {
                    // Skip GeneralSkills
                }

                @Override
                public <T extends EntityTargetedSkill<?>> void register(Class<T> skillClass,
                        EntityTargetedSkill.Meta<T> meta) {
                    try {
                        // Get the entity class from the skill's generic type parameter
                        Class<? extends Entity> entityClass = Misc.getClazzGeneric(
                                skillClass, EntityTargetedSkill.class, 0).asSubclass(Entity.class);

                        String skillSimpleName = skillClass.getSimpleName();
                        String entitySimpleName = entityClass.getSimpleName();

                        // Check if entity class name is a prefix of skill class name
                        // (e.g., "Entity" should be prefix of "EntitySetInvulnerableSkill")
                        if (!skillSimpleName.startsWith(entitySimpleName)) {
                            throw new IllegalStateException(
                                    String.format(
                                        "Naming convention violation: Skill class '%s' should start with "
                                            + "entity class name '%s'",
                                        skillSimpleName, entitySimpleName
                                    ));
                        }

                        LOGGER.trace("Naming validation passed: {} -> {}", entitySimpleName, skillSimpleName);
                    } catch (Exception e) {
                        helper.fail("Naming validation failed for " + skillClass.getSimpleName()
                                + ": " + Misc.getStackToString(e));
                    }
                }
            });

            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail("testSkillNamingConvention failed: " + Misc.getStackToString(throwable));
        }
    }

    public void testSkillFactoryMapping(GameTestHelper helper) {
        try {
            Set<Class<?>> allSkillClasses = new HashSet<>();
            for (List<Class<? extends EntityTargetedSkill<?>>> skills : ENTITY_TO_SKILLS.values()) {
                allSkillClasses.addAll(skills);
            }

            for (Class<?> skillClass : allSkillClasses) {
                if (!SKILL_TO_FACTORY.containsKey(skillClass) || SKILL_TO_FACTORY.get(skillClass).isEmpty()) {
                    throw new IllegalStateException("No factory registered for skill class: " + skillClass.getName());
                }
            }

            LOGGER.info("Skill factory mapping validated: {} skill classes have factories", allSkillClasses.size());
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail("testSkillFactoryMapping failed: " + Misc.getStackToString(throwable));
        }
    }

    public void testAllSkills(GameTestHelper helper) {
        try {
            ServerLevel level = helper.getLevel();
            ServerPlayer player = createTestPlayer(level);

            if (player == null) {
                LOGGER.warn("Skipping skill tests due to no test player available");
                helper.succeed();
                return;
            }

            // Test with various entity types
            EntityType<?>[] testEntityTypes = {
                EntityType.CHICKEN,
                EntityType.COW,
                EntityType.SHEEP,
                EntityType.PIG,
                EntityType.BEE,
                EntityType.VILLAGER,
                EntityType.WANDERING_TRADER,
                EntityType.ZOMBIE,
                EntityType.CREEPER,
                EntityType.WOLF,
                EntityType.HORSE,
                EntityType.PARROT
            };

            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;

            for (EntityType<?> entityType : testEntityTypes) {
                try {
                    Entity entity = EntityUtils.create(entityType, level);
                    if (entity == null) {
                        skipCount++;
                        LOGGER.debug("Skipped entity type (cannot create): {}", EntityType.getKey(entityType));
                        continue;
                    }

                    int[] results = testSkillsForEntity(entity, player);
                    successCount += results[0];
                    skipCount += results[1];
                    failCount += results[2];
                } catch (Exception e) {
                    failCount++;
                    LOGGER.error("Failed to test skills for entity type: " + EntityType.getKey(entityType), e);
                }
            }

            LOGGER.info("Skill test completed: {} passed, {} skipped, {} failed", successCount, skipCount, failCount);
            if (failCount > 0) {
                helper.fail("Some skills failed: " + failCount + " failures");
            } else {
                helper.succeed();
            }
        } catch (Throwable throwable) {
            helper.fail("testAllSkills failed: " + Misc.getStackToString(throwable));
        }
    }

    private ServerPlayer createTestPlayer(ServerLevel level) {
        // Create a simple test player in creative mode
        CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        return new ServerPlayer(level.getServer(), level,
                commonListenerCookie.gameProfile(), commonListenerCookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.CREATIVE;
            }
        };
    }

    private int[] testSkillsForEntity(Entity entity, ServerPlayer player) {
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;

        // Get applicable skills for this entity type
        List<Class<? extends Entity>> entityClasses = EntityUtils.bottomUp(entity.getClass());
        Set<Class<? extends EntityTargetedSkill<?>>> applicableSkills = new HashSet<>();

        for (Class<? extends Entity> entityClass : entityClasses) {
            List<Class<? extends EntityTargetedSkill<?>>> skills = ENTITY_TO_SKILLS.get(entityClass);
            if (skills != null) {
                applicableSkills.addAll(skills);
            }
        }

        if (applicableSkills.isEmpty()) {
            LOGGER.debug("No applicable skills for entity type: {}", entity.getType());
            return new int[]{0, 1, 0}; // All skipped
        }

        // Test each applicable skill
        for (Class<? extends EntityTargetedSkill<?>> skillClass : applicableSkills) {
            List<EntitySkillFactory<?, ?>> factories = SKILL_TO_FACTORY.get(skillClass);
            if (factories == null || factories.isEmpty()) {
                failCount++;
                LOGGER.error("No factory found for skill class: " + skillClass.getName());
                continue;
            }

            // Test each factory for this skill (e.g., true/false variants)
            for (EntitySkillFactory<?, ?> factory : factories) {
                try {
                    // Check if this skill should be skipped for this entity
                    if (factory.shouldSkip(Misc.cast(entity))) {
                        skipCount++;
                        LOGGER.debug("Skipped skill {} for entity type: {}",
                            skillClass.getSimpleName(), entity.getType());
                        continue;
                    }

                    // Create and execute the skill
                    EntityTargetedSkill<?> skill = factory.create(Misc.cast(entity), player);
                    executeEntityTargetedSkill(skill, entity, player);
                    successCount++;
                    LOGGER.debug("Successfully tested {} for entity type: {}",
                        skillClass.getSimpleName(), entity.getType());

                } catch (Exception e) {
                    failCount++;
                    LOGGER.error("Failed to test skill {} for entity type: {}",
                        skillClass.getSimpleName(), entity.getType(), e);
                }
            }
        }

        return new int[]{successCount, skipCount, failCount};
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity> void executeEntityTargetedSkill(EntityTargetedSkill<?> skill, E entity,
                                                               ServerPlayer player) {
        try {
            EntityTargetedSkill.ServerContext<E> skillCtx
                = new EntityTargetedSkill.ServerContext<>(player.level().getServer(), player, entity);
            ((EntityTargetedSkill<E>) skill).serverAdditionalCheck(skillCtx);

            SkillCost cost = ((EntityTargetedSkill<E>) skill).getRealCost(entity);
            SkillCost.ServerContext costCtx = new SkillCost.ServerContext(player);
            cost.serverCheck(costCtx);
            cost.serverConsume(costCtx);

            ((EntityTargetedSkill<E>) skill).serverDo(skillCtx);
        } catch (NoPermissionException e) {
            LOGGER.debug("NoPermissionException caught (expected): {}", e.getMessage());
        } catch (NullPointerException e) {
            // Ignore NPE caused by null player.connection in test environment
            if (e.getMessage() != null && e.getMessage().contains("\"player.connection\" is null")) {
                LOGGER.debug("Ignoring NPE due to null player.connection in test environment");
            } else {
                throw new RuntimeException("Unexpected NPE during skill execution for entity " + entity.getType(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Skill execution failed for entity " + entity.getType(), e);
        }
    }
}

package io.github.xienaoban.biologydictionary;

import io.github.xienaoban.biologydictionary.core.EntitySpawnManager;
import io.github.xienaoban.biologydictionary.core.session.ServerWorldSession;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.Set;

public class EntitySpawnManagerTest {

    private EntitySpawnManager getManager(GameTestHelper helper) {
        ServerWorldSession sws = ServerWorldSession.get();
        if (sws == null) {
            helper.fail("ServerWorldSession not initialized");
            return null;
        }
        return sws.getEntitySpawnManager();
    }

    // ---- Entity → Biome/Structure tests ----

    public void testGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.GUARDIAN);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("monument")),
            "Guardian should spawn in ocean monuments");
        helper.succeed();
    }

    public void testElderGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.ELDER_GUARDIAN);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("monument")),
            "Elder guardian should spawn in ocean monuments (via data pack override)");
        helper.succeed();
    }

    public void testWardenSpawnsInAncientCity(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.WARDEN);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("ancient_city")),
            "Warden should spawn in ancient city (via data pack override)");
        helper.succeed();
    }

    public void testCreakingSpawnsInPaleGarden(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        EntityType<?> creaking = EntityType.byString("minecraft:creaking").orElse(null);
        if (creaking == null) {
            helper.succeed();
            return;
        }
        Set<ResourceLocation> biomes = manager.getSpawnBiomes(creaking);
        helper.assertTrue(biomes.contains(ResourceLocation.withDefaultNamespace("pale_garden")),
            "Creaking should spawn in pale garden (via data pack override)");
        helper.succeed();
    }

    public void testEnderDragonSpawnsInTheEnd(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> biomes = manager.getSpawnBiomes(EntityType.ENDER_DRAGON);
        helper.assertTrue(biomes.contains(ResourceLocation.withDefaultNamespace("the_end")),
            "Ender dragon should spawn in the end (via data pack override)");
        helper.succeed();
    }

    public void testVexSpawnsInMansion(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.VEX);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("mansion")),
            "Vex should spawn in mansion (via data pack override)");
        helper.succeed();
    }

    public void testBreezeSpawnsInTrialChambers(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.BREEZE);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("trial_chambers")),
            "Breeze should spawn in trial chambers (via data pack override)");
        helper.succeed();
    }

    public void testCaveSpiderSpawnsInMineshaft(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.CAVE_SPIDER);
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("mineshaft")),
            "Cave spider should spawn in mineshaft (via data pack override)");
        helper.succeed();
    }

    public void testBeeSpawnsInFlowerForest(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<ResourceLocation> biomes = manager.getSpawnBiomes(EntityType.BEE);
        helper.assertTrue(biomes.contains(ResourceLocation.withDefaultNamespace("flower_forest")),
            "Bee should spawn in flower forest (via data pack override)");
        helper.succeed();
    }

    // ---- Biome/Structure → Entity tests ----

    public void testOceanMonumentHasGuardians(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<EntityType<?>> entities = manager.getStructureEntities(ResourceLocation.withDefaultNamespace("monument"));
        helper.assertTrue(entities.contains(EntityType.GUARDIAN),
            "Ocean monument should have guardians");
        helper.assertTrue(entities.contains(EntityType.ELDER_GUARDIAN),
            "Ocean monument should have elder guardians (via data pack override)");
        helper.succeed();
    }

    public void testPlainsHasChicken(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<EntityType<?>> entities = manager.getBiomeEntities(ResourceLocation.withDefaultNamespace("plains"));
        helper.assertTrue(entities.contains(EntityType.CHICKEN),
            "Plains should have chickens");
        helper.succeed();
    }

    public void testAncientCityHasWarden(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<EntityType<?>> entities = manager.getStructureEntities(ResourceLocation.withDefaultNamespace("ancient_city"));
        helper.assertTrue(entities.contains(EntityType.WARDEN),
            "Ancient city should have warden (via data pack override)");
        helper.succeed();
    }

    public void testFlowerForestHasBee(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        Set<EntityType<?>> entities = manager.getBiomeEntities(ResourceLocation.withDefaultNamespace("flower_forest"));
        helper.assertTrue(entities.contains(EntityType.BEE),
            "Flower forest should have bee (via data pack override)");
        helper.succeed();
    }

    public void testCatSpawnsOnlyInVillagesAndWitchHut(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        // Cat should have NO biomes
        Set<ResourceLocation> biomes = manager.getSpawnBiomes(EntityType.CAT);
        helper.assertTrue(biomes.isEmpty(), "Cat should not spawn in any biome");
        // Cat should only have village structures + witch hut
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.CAT);
        boolean hasVillage = false;
        for (ResourceLocation id : structures) {
            String name = id.toString();
            if (name.contains("village")) {
                hasVillage = true;
            } else if (name.equals("minecraft:swamp_hut")) {
                // expected
            } else {
                helper.fail("Cat should only spawn in villages and witch huts, found: " + id);
                return;
            }
        }
        helper.assertTrue(hasVillage, "Cat should spawn in villages");
        helper.assertTrue(structures.contains(ResourceLocation.withDefaultNamespace("swamp_hut")),
            "Cat should spawn in witch huts");
        helper.succeed();
    }

    public void testParrotSpawnsOnlyInJungleAndBambooJungle(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        // Parrot should have NO structures
        Set<ResourceLocation> structures = manager.getSpawnStructures(EntityType.PARROT);
        helper.assertTrue(structures.isEmpty(), "Parrot should not spawn in any structure");
        // Parrot should only have jungle + bamboo_jungle biomes
        Set<ResourceLocation> biomes = manager.getSpawnBiomes(EntityType.PARROT);
        for (ResourceLocation id : biomes) {
            String name = id.toString();
            if (!name.equals("minecraft:jungle") && !name.equals("minecraft:bamboo_jungle")) {
                helper.fail("Parrot should only spawn in jungle and bamboo jungle, found biome: " + id);
                return;
            }
        }
        helper.assertTrue(biomes.contains(ResourceLocation.withDefaultNamespace("jungle")),
            "Parrot should spawn in jungle");
        helper.assertTrue(biomes.contains(ResourceLocation.withDefaultNamespace("bamboo_jungle")),
            "Parrot should spawn in bamboo jungle");
        helper.succeed();
    }

    // ---- Forward/Reverse consistency tests ----

    public void testAllBiomeIdentifiersExist(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        RegistryAccess registries = helper.getLevel().getServer().registryAccess();
        var biomeRegistry = registries.registryOrThrow(Registries.BIOME);
        for (EntityType<?> entityType : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            for (ResourceLocation id : manager.getSpawnBiomes(entityType)) {
                if (biomeRegistry.get(id) == null) {
                    helper.fail("Biome identifier not found in registry: " + id);
                    return;
                }
            }
        }
        helper.succeed();
    }

    public void testAllStructureIdentifiersExist(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        RegistryAccess registries = helper.getLevel().getServer().registryAccess();
        var structureRegistry = registries.registryOrThrow(Registries.STRUCTURE);
        for (EntityType<?> entityType : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            for (ResourceLocation id : manager.getSpawnStructures(entityType)) {
                if (structureRegistry.get(id) == null) {
                    helper.fail("Structure identifier not found in registry: " + id);
                    return;
                }
            }
        }
        helper.succeed();
    }

    public void testBiomeForwardReverseConsistent(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        checkForwardReverseConsistency(helper, manager, false);
        helper.succeed();
    }

    public void testStructureForwardReverseConsistent(GameTestHelper helper) {
        EntitySpawnManager manager = getManager(helper);
        if (manager == null) return;
        checkForwardReverseConsistency(helper, manager, true);
        helper.succeed();
    }

    private void checkForwardReverseConsistency(GameTestHelper helper, EntitySpawnManager manager, boolean structure) {
        // forward → reverse: for each entity, check that reverse contains the entity
        for (EntityType<?> entityType : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            java.util.Set<ResourceLocation> ids = structure
                ? manager.getSpawnStructures(entityType)
                : manager.getSpawnBiomes(entityType);
            for (ResourceLocation id : ids) {
                java.util.Set<EntityType<?>> reverse = structure
                    ? manager.getStructureEntities(id)
                    : manager.getBiomeEntities(id);
                if (!reverse.contains(entityType)) {
                    helper.fail((structure ? "Structure" : "Biome") + " reverse missing: "
                        + entityType + " <-> " + id);
                    return;
                }
            }
        }
        // reverse → forward: collect all ids from reverse, check forward
        java.util.Set<ResourceLocation> checkedIds = new java.util.HashSet<>();
        for (EntityType<?> entityType : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            java.util.Set<ResourceLocation> ids = structure
                ? manager.getSpawnStructures(entityType)
                : manager.getSpawnBiomes(entityType);
            checkedIds.addAll(ids);
        }
        for (ResourceLocation id : checkedIds) {
            java.util.Set<EntityType<?>> entities = structure
                ? manager.getStructureEntities(id)
                : manager.getBiomeEntities(id);
            for (EntityType<?> entityType : entities) {
                java.util.Set<ResourceLocation> forward = structure
                    ? manager.getSpawnStructures(entityType)
                    : manager.getSpawnBiomes(entityType);
                if (!forward.contains(id)) {
                    helper.fail((structure ? "Structure" : "Biome") + " forward missing: "
                        + entityType + " <-> " + id);
                    return;
                }
            }
        }
    }
}

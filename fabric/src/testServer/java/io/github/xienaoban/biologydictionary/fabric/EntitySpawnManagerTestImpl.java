package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.EntitySpawnManagerTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class EntitySpawnManagerTestImpl implements FabricGameTest {
    private final EntitySpawnManagerTest test = new EntitySpawnManagerTest();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        test.testGuardianSpawnsInOceanMonument(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testElderGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        test.testElderGuardianSpawnsInOceanMonument(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testWardenSpawnsInAncientCity(GameTestHelper helper) {
        test.testWardenSpawnsInAncientCity(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testCreakingSpawnsInPaleGarden(GameTestHelper helper) {
        test.testCreakingSpawnsInPaleGarden(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testEnderDragonSpawnsInTheEnd(GameTestHelper helper) {
        test.testEnderDragonSpawnsInTheEnd(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testVexSpawnsInMansion(GameTestHelper helper) {
        test.testVexSpawnsInMansion(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testBreezeSpawnsInTrialChambers(GameTestHelper helper) {
        test.testBreezeSpawnsInTrialChambers(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testCaveSpiderSpawnsInMineshaft(GameTestHelper helper) {
        test.testCaveSpiderSpawnsInMineshaft(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testBeeSpawnsInFlowerForest(GameTestHelper helper) {
        test.testBeeSpawnsInFlowerForest(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testOceanMonumentHasGuardians(GameTestHelper helper) {
        test.testOceanMonumentHasGuardians(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testPlainsHasChicken(GameTestHelper helper) {
        test.testPlainsHasChicken(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAncientCityHasWarden(GameTestHelper helper) {
        test.testAncientCityHasWarden(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testFlowerForestHasBee(GameTestHelper helper) {
        test.testFlowerForestHasBee(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testCatSpawnsOnlyInVillagesAndWitchHut(GameTestHelper helper) {
        test.testCatSpawnsOnlyInVillagesAndWitchHut(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testParrotSpawnsOnlyInJungleAndBambooJungle(GameTestHelper helper) {
        test.testParrotSpawnsOnlyInJungleAndBambooJungle(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllBiomeIdentifiersExist(GameTestHelper helper) {
        test.testAllBiomeIdentifiersExist(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testAllStructureIdentifiersExist(GameTestHelper helper) {
        test.testAllStructureIdentifiersExist(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testBiomeForwardReverseConsistent(GameTestHelper helper) {
        test.testBiomeForwardReverseConsistent(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testStructureForwardReverseConsistent(GameTestHelper helper) {
        test.testStructureForwardReverseConsistent(helper);
    }
}

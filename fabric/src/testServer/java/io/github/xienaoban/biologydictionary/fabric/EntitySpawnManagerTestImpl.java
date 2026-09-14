package io.github.xienaoban.biologydictionary.fabric;

import io.github.xienaoban.biologydictionary.EntitySpawnManagerTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class EntitySpawnManagerTestImpl {
    private final EntitySpawnManagerTest test = new EntitySpawnManagerTest();

    @GameTest
    public void testGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        test.testGuardianSpawnsInOceanMonument(helper);
    }

    @GameTest
    public void testElderGuardianSpawnsInOceanMonument(GameTestHelper helper) {
        test.testElderGuardianSpawnsInOceanMonument(helper);
    }

    @GameTest
    public void testWardenSpawnsInAncientCity(GameTestHelper helper) {
        test.testWardenSpawnsInAncientCity(helper);
    }

    @GameTest
    public void testCreakingSpawnsInPaleGarden(GameTestHelper helper) {
        test.testCreakingSpawnsInPaleGarden(helper);
    }

    @GameTest
    public void testEnderDragonSpawnsInTheEnd(GameTestHelper helper) {
        test.testEnderDragonSpawnsInTheEnd(helper);
    }

    @GameTest
    public void testVexSpawnsInMansion(GameTestHelper helper) {
        test.testVexSpawnsInMansion(helper);
    }

    @GameTest
    public void testBreezeSpawnsInTrialChambers(GameTestHelper helper) {
        test.testBreezeSpawnsInTrialChambers(helper);
    }

    @GameTest
    public void testCaveSpiderSpawnsInMineshaft(GameTestHelper helper) {
        test.testCaveSpiderSpawnsInMineshaft(helper);
    }

    @GameTest
    public void testBeeSpawnsInFlowerForest(GameTestHelper helper) {
        test.testBeeSpawnsInFlowerForest(helper);
    }

    @GameTest
    public void testOceanMonumentHasGuardians(GameTestHelper helper) {
        test.testOceanMonumentHasGuardians(helper);
    }

    @GameTest
    public void testPlainsHasChicken(GameTestHelper helper) {
        test.testPlainsHasChicken(helper);
    }

    @GameTest
    public void testAncientCityHasWarden(GameTestHelper helper) {
        test.testAncientCityHasWarden(helper);
    }

    @GameTest
    public void testFlowerForestHasBee(GameTestHelper helper) {
        test.testFlowerForestHasBee(helper);
    }

    @GameTest
    public void testCatSpawnsOnlyInVillagesAndWitchHut(GameTestHelper helper) {
        test.testCatSpawnsOnlyInVillagesAndWitchHut(helper);
    }

    @GameTest
    public void testParrotSpawnsOnlyInJungleAndBambooJungle(GameTestHelper helper) {
        test.testParrotSpawnsOnlyInJungleAndBambooJungle(helper);
    }

    @GameTest
    public void testAllBiomeIdentifiersExist(GameTestHelper helper) {
        test.testAllBiomeIdentifiersExist(helper);
    }

    @GameTest
    public void testAllStructureIdentifiersExist(GameTestHelper helper) {
        test.testAllStructureIdentifiersExist(helper);
    }

    @GameTest
    public void testBiomeForwardReverseConsistent(GameTestHelper helper) {
        test.testBiomeForwardReverseConsistent(helper);
    }

    @GameTest
    public void testStructureForwardReverseConsistent(GameTestHelper helper) {
        test.testStructureForwardReverseConsistent(helper);
    }
}

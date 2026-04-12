package io.github.xienaoban.biologydictionary.core;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

/**
 * Retrieves bidirectional spawn information between entity types and biomes/structures.
 * <ul>
 *   <li>Entity <-> Biomes: natural spawn mapping</li>
 *   <li>Entity <-> Structures: structure spawn overrides mapping</li>
 * </ul>
 * Requires {@link RegistryAccess} containing WORLDGEN-layer registries (i.e. server-side).
 */
public class EntitySpawnManager {
    private final Map<EntityType<?>, List<Entry<Biome>>> spawnBiomes = new HashMap<>();
    private final Map<ResourceLocation, List<EntityType<?>>> biomeEntities = new HashMap<>();

    private final Map<EntityType<?>, List<Entry<Structure>>> spawnStructures = new HashMap<>();
    private final Map<ResourceLocation, List<EntityType<?>>> structureEntities = new HashMap<>();

    public EntitySpawnManager(RegistryAccess registryAccess) {
        buildSpawnBiomes(registryAccess);
        buildSpawnStructures(registryAccess);
    }

    public List<Entry<Biome>> getSpawnBiomes(EntityType<?> entityType) {
        return spawnBiomes.getOrDefault(entityType, ImmutableList.of());
    }

    public List<EntityType<?>> getBiomeEntities(ResourceLocation biomeId) {
        return biomeEntities.getOrDefault(biomeId, ImmutableList.of());
    }

    public List<Entry<Structure>> getSpawnStructures(EntityType<?> entityType) {
        return spawnStructures.getOrDefault(entityType, ImmutableList.of());
    }

    public List<EntityType<?>> getStructureEntities(ResourceLocation structureId) {
        return structureEntities.getOrDefault(structureId, ImmutableList.of());
    }

    private void buildSpawnBiomes(RegistryAccess registryAccess) {
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : biomeRegistry.entrySet()) {
            ResourceLocation biomeId = biomeEntry.getKey().location();
            Biome biome = biomeEntry.getValue();
            Entry<Biome> entry = new Entry<>(biomeId, biome);
            MobSpawnSettings spawnSettings = biome.getMobSettings();

            for (MobCategory category : MobCategory.values()) {
                WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = spawnSettings.getMobs(category);
                for (MobSpawnSettings.SpawnerData spawnerData : spawners.unwrap()) {
                    EntityType<?> entityType = spawnerData.type;

                    spawnBiomes.computeIfAbsent(entityType, k -> new ArrayList<>())
                        .add(entry);
                    biomeEntities.computeIfAbsent(biomeId, k -> new ArrayList<>())
                        .add(entityType);
                }
            }
        }
    }

    private void buildSpawnStructures(RegistryAccess registryAccess) {
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureRegistry.entrySet()) {
            ResourceLocation structureId = structureEntry.getKey().location();
            Structure structure = structureEntry.getValue();
            Entry<Structure> entry = new Entry<>(structureId, structure);

            structure.spawnOverrides().forEach((category, override) -> {
                for (MobSpawnSettings.SpawnerData spawnerData : override.spawns().unwrap()) {
                    EntityType<?> entityType = spawnerData.type;

                    spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                        .add(entry);
                    structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                        .add(entityType);
                }
            });
        }
    }

    public record Entry<T>(ResourceLocation id, T value) {}
}

package io.github.xienaoban.biologydictionary.core;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;

/**
 * Retrieves bidirectional spawn information between entity types and biomes/structures.
 * <ul>
 *   <li>Entity ↔ Biomes: natural spawn mapping</li>
 *   <li>Entity ↔ Structures: structure spawn overrides mapping</li>
 * </ul>
 * Requires {@link RegistryAccess} containing WORLDGEN-layer registries (i.e. server-side).
 */
public class EntitySpawnManager {
    private final Map<EntityType<?>, List<Entry<Biome>>> spawnBiomes = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> biomeEntities = new HashMap<>();

    private final Map<EntityType<?>, List<Entry<Structure>>> spawnStructures = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> structureEntities = new HashMap<>();

    public EntitySpawnManager(RegistryAccess registryAccess) {
        buildSpawnBiomes(registryAccess);
        buildSpawnStructures(registryAccess);
    }

    public List<Entry<Biome>> getSpawnBiomes(EntityType<?> entityType) {
        return spawnBiomes.getOrDefault(entityType, ImmutableList.of());
    }

    public List<EntityType<?>> getBiomeEntities(Identifier biomeId) {
        return biomeEntities.getOrDefault(biomeId, ImmutableList.of());
    }

    public List<Entry<Structure>> getSpawnStructures(EntityType<?> entityType) {
        return spawnStructures.getOrDefault(entityType, ImmutableList.of());
    }

    public List<EntityType<?>> getStructureEntities(Identifier structureId) {
        return structureEntities.getOrDefault(structureId, ImmutableList.of());
    }

    private void buildSpawnBiomes(RegistryAccess registryAccess) {
        Registry<Biome> biomeRegistry = registryAccess.lookupOrThrow(Registries.BIOME);

        for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : biomeRegistry.entrySet()) {
            Identifier biomeId = biomeEntry.getKey().identifier();
            Biome biome = biomeEntry.getValue();
            Entry<Biome> entry = new Entry<>(biomeId, biome);
            MobSpawnSettings spawnSettings = biome.getMobSettings();

            Set<EntityType<?>> seenBiomeEntities = new HashSet<>();
            for (MobCategory category : MobCategory.values()) {
                WeightedList<MobSpawnSettings.SpawnerData> spawners = spawnSettings.getMobs(category);
                for (Weighted<MobSpawnSettings.SpawnerData> weighted : spawners.unwrap()) {
                    MobSpawnSettings.SpawnerData spawnerData = weighted.value();
                    EntityType<?> entityType = spawnerData.type();

                    if (seenBiomeEntities.add(entityType)) {
                        spawnBiomes.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(entry);
                        biomeEntities.computeIfAbsent(biomeId, k -> new ArrayList<>())
                            .add(entityType);
                    }
                }
            }
        }
    }

    private void buildSpawnStructures(RegistryAccess registryAccess) {
        Registry<Structure> structureRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);

        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureRegistry.entrySet()) {
            Identifier structureId = structureEntry.getKey().identifier();
            Structure structure = structureEntry.getValue();
            Entry<Structure> entry = new Entry<>(structureId, structure);

            Set<EntityType<?>> seenStructureEntities = new HashSet<>();
            structure.spawnOverrides().forEach((category, override) -> {
                for (Weighted<MobSpawnSettings.SpawnerData> weighted : override.spawns().unwrap()) {
                    MobSpawnSettings.SpawnerData spawnerData = weighted.value();
                    EntityType<?> entityType = spawnerData.type();

                    if (seenStructureEntities.add(entityType)) {
                        spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(entry);
                        structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                            .add(entityType);
                    }
                }
            });
        }
    }

    public record Entry<T>(Identifier id, T value) {}
}

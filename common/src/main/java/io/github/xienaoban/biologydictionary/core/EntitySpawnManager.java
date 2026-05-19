package io.github.xienaoban.biologydictionary.core;

import com.google.common.collect.ImmutableList;
import io.github.xienaoban.biologydictionary.mixin.ListPoolElementIMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.io.InputStream;
import java.util.*;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

/**
 * Retrieves bidirectional spawn information between entity types and biomes/structures.
 * <ul>
 *   <li>Entity ↔ Biomes: natural spawn mapping</li>
 *   <li>Entity ↔ Structures: structure spawn overrides + template entity mapping</li>
 * </ul>
 * Requires {@link RegistryAccess} containing WORLDGEN-layer registries (i.e. server-side).
 */
public class EntitySpawnManager {
    private static final FileToIdConverter STRUCTURE_LISTER = new FileToIdConverter("structure", ".nbt");
    private static final CompoundTag MISSING_TEMPLATE = new CompoundTag();

    private final Map<EntityType<?>, List<Entry<Biome>>> spawnBiomes = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> biomeEntities = new HashMap<>();

    private final Map<EntityType<?>, List<Entry<Structure>>> spawnStructures = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> structureEntities = new HashMap<>();

    public EntitySpawnManager(RegistryAccess registryAccess, ResourceManager resourceManager) {
        buildSpawnBiomes(registryAccess);
        buildSpawnStructures(registryAccess, resourceManager);
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
            try {
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
            } catch (Throwable e) {
                LOGGER.warn("Failed to process spawn settings for biome {}", biomeEntry.getKey(), e);
            }
        }
    }

    private void buildSpawnStructures(RegistryAccess registryAccess, ResourceManager resourceManager) {
        Registry<Structure> structureRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        Registry<StructureTemplatePool> poolRegistry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);

        Map<Identifier, CompoundTag> templateCache = new HashMap<>();

        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureRegistry.entrySet()) {
            try {
                Identifier structureId = structureEntry.getKey().identifier();
                Structure structure = structureEntry.getValue();
                Entry<Structure> entry = new Entry<>(structureId, structure);

                Set<EntityType<?>> seenStructureEntities = new HashSet<>();

                // 1. spawnOverrides (e.g. guardians in ocean monuments)
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

                // 2. Template entities for Jigsaw structures (e.g. villagers in villages)
                if (structure instanceof JigsawStructure jigsawStructure) {
                    collectTemplateEntities(
                        jigsawStructure.getStartPool(), poolRegistry, resourceManager, templateCache,
                        structureId, entry, seenStructureEntities
                    );
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to process spawn settings for structure {}", structureEntry.getKey(), e);
            }
        }
    }

    private void collectTemplateEntities(
        Holder<StructureTemplatePool> startPool,
        Registry<StructureTemplatePool> poolRegistry,
        ResourceManager resourceManager,
        Map<Identifier, CompoundTag> templateCache,
        Identifier structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities
    ) {
        Set<Identifier> visitedPools = new HashSet<>();
        Queue<Identifier> poolQueue = new ArrayDeque<>();

        Identifier startPoolId = startPool.unwrapKey()
            .map(ResourceKey::identifier).orElse(null);
        if (startPoolId == null) return;
        visitedPools.add(startPoolId);
        poolQueue.add(startPoolId);

        while (!poolQueue.isEmpty()) {
            Identifier currentPoolId = poolQueue.poll();
            Optional<? extends Holder<StructureTemplatePool>> poolHolder = poolRegistry.get(currentPoolId);
            if (poolHolder.isEmpty()) continue;
            StructureTemplatePool pool = poolHolder.get().value();
            Set<Identifier> referencedPools = new HashSet<>();

            // Collect entities from all templates in this pool
            for (var elementPair : pool.getTemplates()) {
                try {
                    collectElementEntities(
                        elementPair.getFirst(), resourceManager, templateCache,
                        structureId, structureEntry, seenStructureEntities, referencedPools
                    );
                } catch (Exception e) {
                    LOGGER.warn("Failed to process element in template pool {}", currentPoolId, e);
                }
            }

            // Also follow fallback pool
            try {
                Holder<StructureTemplatePool> fallback = pool.getFallback();
                if (fallback.value() != pool) {
                    fallback.unwrapKey().ifPresent(poolKey -> {
                        Identifier fallbackId = poolKey.identifier();
                        if (visitedPools.add(fallbackId)) {
                            poolQueue.add(fallbackId);
                        }
                    });
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to process fallback pool for {}", currentPoolId, e);
            }

            // Follow jigsaw-referenced pools from templates
            for (Identifier poolId : referencedPools) {
                if (visitedPools.add(poolId)) {
                    poolQueue.add(poolId);
                }
            }
        }
    }

    private void collectElementEntities(
        StructurePoolElement element,
        ResourceManager resourceManager,
        Map<Identifier, CompoundTag> templateCache,
        Identifier structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities,
        Set<Identifier> referencedPools
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            Identifier templateId = singlePoolElement.getTemplateLocation();
            if (templateId == null) return;

            CompoundTag rootNbt = templateCache.computeIfAbsent(templateId, id -> {
                try {
                    Identifier resourceLoc = STRUCTURE_LISTER.idToFile(id);
                    try (InputStream is = resourceManager.open(resourceLoc)) {
                        return NbtIo.readCompressed(is, NbtAccounter.create(64 * 1024 * 1024));
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to read structure template {}", id, e);
                    return MISSING_TEMPLATE;
                }
            });
            if (rootNbt == MISSING_TEMPLATE) { return; }

            // Extract entities
            ListTag entities = rootNbt.getListOrEmpty("entities");
            for (int i = 0; i < entities.size(); i++) {
                CompoundTag entityTag = entities.getCompoundOrEmpty(i);
                CompoundTag nbt = entityTag.getCompoundOrEmpty("nbt");
                nbt.getString("id").flatMap(EntityType::byString).ifPresent(entityType -> {
                    if (seenStructureEntities.add(entityType)) {
                        spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(structureEntry);
                        structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                            .add(entityType);
                    }
                });
            }

            // Find jigsaw-referenced pools from blocks (only jigsaw blocks have "pool" in their nbt)
            ListTag blocks = rootNbt.getListOrEmpty("blocks");
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag blockTag = blocks.getCompoundOrEmpty(i);
                CompoundTag blockNbt = blockTag.getCompoundOrEmpty("nbt");
                String poolStr = blockNbt.getStringOr("pool", "");
                if (!poolStr.isEmpty()) {
                    Identifier poolLoc = Identifier.tryParse(poolStr);
                    if (poolLoc != null && !poolLoc.equals(Identifier.withDefaultNamespace("empty"))) {
                        referencedPools.add(poolLoc);
                    }
                }
            }
        } else if (element instanceof ListPoolElement listPoolElement) {
            for (StructurePoolElement child : ((ListPoolElementIMixin) listPoolElement).biologydictionary$getElements()) {
                collectElementEntities(child, resourceManager, templateCache,
                    structureId, structureEntry, seenStructureEntities, referencedPools);
            }
        }
    }

    public record Entry<T>(Identifier id, T value) {}
}

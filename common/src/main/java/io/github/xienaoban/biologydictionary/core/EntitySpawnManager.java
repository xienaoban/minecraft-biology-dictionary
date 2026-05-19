package io.github.xienaoban.biologydictionary.core;

import com.google.common.collect.ImmutableList;
import io.github.xienaoban.biologydictionary.mixin.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.random.WeightedRandomList;
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
 *   <li>Entity <-> Biomes: natural spawn mapping</li>
 *   <li>Entity <-> Structures: structure spawn overrides + template entity mapping</li>
 * </ul>
 * Requires {@link RegistryAccess} containing WORLDGEN-layer registries (i.e. server-side).
 */
public class EntitySpawnManager {
    private static final FileToIdConverter STRUCTURE_LISTER = new FileToIdConverter("structures", ".nbt");
    private static final CompoundTag MISSING_TEMPLATE = new CompoundTag();

    private final Map<EntityType<?>, List<Entry<Biome>>> spawnBiomes = new HashMap<>();
    private final Map<ResourceLocation, List<EntityType<?>>> biomeEntities = new HashMap<>();

    private final Map<EntityType<?>, List<Entry<Structure>>> spawnStructures = new HashMap<>();
    private final Map<ResourceLocation, List<EntityType<?>>> structureEntities = new HashMap<>();

    public EntitySpawnManager(RegistryAccess registryAccess, ResourceManager resourceManager) {
        buildSpawnBiomes(registryAccess);
        buildSpawnStructures(registryAccess, resourceManager);
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
            try {
                ResourceLocation biomeId = biomeEntry.getKey().location();
                Biome biome = biomeEntry.getValue();
                Entry<Biome> entry = new Entry<>(biomeId, biome);
                MobSpawnSettings spawnSettings = biome.getMobSettings();

                Set<EntityType<?>> seenBiomeEntities = new HashSet<>();
                for (MobCategory category : MobCategory.values()) {
                    WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = spawnSettings.getMobs(category);
                    for (MobSpawnSettings.SpawnerData spawnerData : spawners.unwrap()) {
                        EntityType<?> entityType = spawnerData.type;

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
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureTemplatePool> poolRegistry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);

        Map<ResourceLocation, CompoundTag> templateCache = new HashMap<>();

        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureRegistry.entrySet()) {
            try {
                ResourceLocation structureId = structureEntry.getKey().location();
                Structure structure = structureEntry.getValue();
                Entry<Structure> entry = new Entry<>(structureId, structure);

                Set<EntityType<?>> seenStructureEntities = new HashSet<>();

                // 1. spawnOverrides (e.g. guardians in ocean monuments)
                structure.spawnOverrides().forEach((category, override) -> {
                    for (MobSpawnSettings.SpawnerData spawnerData : override.spawns().unwrap()) {
                        EntityType<?> entityType = spawnerData.type;

                        if (seenStructureEntities.add(entityType)) {
                            spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                                .add(entry);
                            structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                                .add(entityType);
                        }
                    }
                });

                // 2. Template entities for Jigsaw structures (e.g. villagers in villages)
                if (structure instanceof JigsawStructure) {
                    Holder<StructureTemplatePool> startPool = ((JigsawStructureIMixin) (Object) structure).biologydictionary$getStartPool();
                    collectTemplateEntities(
                        startPool, poolRegistry, resourceManager, templateCache,
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
        Map<ResourceLocation, CompoundTag> templateCache,
        ResourceLocation structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities
    ) {
        Set<ResourceLocation> visitedPools = new HashSet<>();
        Queue<ResourceLocation> poolQueue = new ArrayDeque<>();

        ResourceLocation startPoolId = startPool.unwrapKey()
            .map(ResourceKey::location).orElse(null);
        if (startPoolId == null) return;
        visitedPools.add(startPoolId);
        poolQueue.add(startPoolId);

        while (!poolQueue.isEmpty()) {
            ResourceLocation currentPoolId = poolQueue.poll();
            Optional<StructureTemplatePool> poolHolder = poolRegistry.getOptional(currentPoolId);
            if (poolHolder.isEmpty()) continue;
            StructureTemplatePool pool = poolHolder.get();
            Set<ResourceLocation> referencedPools = new HashSet<>();

            // Collect entities from all templates in this pool
            for (StructurePoolElement element : ((StructureTemplatePoolIMixin) pool).biologydictionary$getTemplates()) {
                try {
                    collectElementEntities(
                        element, resourceManager, templateCache,
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
                        ResourceLocation fallbackId = poolKey.location();
                        if (visitedPools.add(fallbackId)) {
                            poolQueue.add(fallbackId);
                        }
                    });
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to process fallback pool for {}", currentPoolId, e);
            }

            // Follow jigsaw-referenced pools from templates
            for (ResourceLocation poolId : referencedPools) {
                if (visitedPools.add(poolId)) {
                    poolQueue.add(poolId);
                }
            }
        }
    }

    private void collectElementEntities(
        StructurePoolElement element,
        ResourceManager resourceManager,
        Map<ResourceLocation, CompoundTag> templateCache,
        ResourceLocation structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities,
        Set<ResourceLocation> referencedPools
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            ResourceLocation templateId = getTemplateLocation(singlePoolElement);
            if (templateId == null) return;

            CompoundTag rootNbt = templateCache.computeIfAbsent(templateId, id -> {
                try {
                    ResourceLocation resourceLoc = STRUCTURE_LISTER.idToFile(id);
                    try (InputStream is = resourceManager.open(resourceLoc)) {
                        return NbtIo.readCompressed(is);
                    }
                } catch (Throwable e) {
                    LOGGER.warn("Failed to read structure template {}", id, e);
                    return MISSING_TEMPLATE;
                }
            });
            if (rootNbt == MISSING_TEMPLATE) { return; }

            // Extract entities
            ListTag entities = rootNbt.getList("entities", 10);
            for (int i = 0; i < entities.size(); i++) {
                CompoundTag entityTag = entities.getCompound(i);
                if (!entityTag.contains("nbt")) continue;
                CompoundTag nbt = entityTag.getCompound("nbt");
                if (!nbt.contains("id")) continue;
                String id = nbt.getString("id");
                EntityType.byString(id).ifPresent(entityType -> {
                    if (seenStructureEntities.add(entityType)) {
                        spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(structureEntry);
                        structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                            .add(entityType);
                    }
                });
            }

            // Find jigsaw-referenced pools from blocks (only jigsaw blocks have "pool" in their nbt)
            ListTag blocks = rootNbt.getList("blocks", 10);
            for (int i = 0; i < blocks.size(); i++) {
                CompoundTag blockTag = blocks.getCompound(i);
                if (!blockTag.contains("nbt")) continue;
                CompoundTag blockNbt = blockTag.getCompound("nbt");
                String poolStr = blockNbt.getString("pool");
                if (!poolStr.isEmpty()) {
                    ResourceLocation poolLoc = ResourceLocation.tryParse(poolStr);
                    if (poolLoc != null && !poolLoc.equals(new ResourceLocation("minecraft", "empty"))) {
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

    private static ResourceLocation getTemplateLocation(SinglePoolElement element) {
        return ((SinglePoolElementIMixin) element).biologydictionary$getTemplate().left().orElse(null);
    }

    public record Entry<T>(ResourceLocation id, T value) {}
}

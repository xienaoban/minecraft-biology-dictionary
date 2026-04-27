package io.github.xienaoban.biologydictionary.core;

import com.google.common.collect.ImmutableList;
import io.github.xienaoban.biologydictionary.BiologyDictionary;
import io.github.xienaoban.biologydictionary.mixin.ListPoolElementIMixin;
import io.github.xienaoban.biologydictionary.mixin.StructureTemplateIMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

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
    private final Map<EntityType<?>, List<Entry<Biome>>> spawnBiomes = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> biomeEntities = new HashMap<>();

    private final Map<EntityType<?>, List<Entry<Structure>>> spawnStructures = new HashMap<>();
    private final Map<Identifier, List<EntityType<?>>> structureEntities = new HashMap<>();

    public EntitySpawnManager(RegistryAccess registryAccess, StructureTemplateManager templateManager) {
        buildSpawnBiomes(registryAccess);
        buildSpawnStructures(registryAccess, templateManager);
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

    private void buildSpawnStructures(RegistryAccess registryAccess, StructureTemplateManager templateManager) {
        Registry<Structure> structureRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        Registry<StructureTemplatePool> poolRegistry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);

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
                        jigsawStructure.getStartPool(), poolRegistry, templateManager,
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
        StructureTemplateManager templateManager,
        Identifier structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities
    ) {
        Set<Identifier> visitedPools = new HashSet<>();
        Queue<StructureTemplatePool> queue = new ArrayDeque<>();

        Identifier startPoolId = startPool.unwrapKey()
            .map(ResourceKey::identifier).orElse(null);
        if (startPoolId == null) return;
        visitedPools.add(startPoolId);
        queue.add(startPool.value());

        while (!queue.isEmpty()) {
            StructureTemplatePool pool = queue.poll();

            // Collect entities from all templates in this pool
            for (var elementPair : pool.getTemplates()) {
                try {
                    collectElementEntities(
                        elementPair.getFirst(), templateManager, structureId, structureEntry, seenStructureEntities
                    );
                } catch (Exception e) {
                    LOGGER.warn("Failed to process element in template pool {}", startPoolId, e);
                }
            }

            // Discover referenced pools via jigsaw blocks in templates
            Set<ResourceKey<StructureTemplatePool>> referencedPoolKeys = new HashSet<>();
            for (var elementPair : pool.getTemplates()) {
                collectReferencedPools(elementPair.getFirst(), templateManager, referencedPoolKeys);
            }

            // Also follow fallback pool
            try {
                Holder<StructureTemplatePool> fallback = pool.getFallback();
                if (fallback.value() != pool) {
                    fallback.unwrapKey().ifPresent(referencedPoolKeys::add);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to process fallback pool for {}", startPoolId, e);
            }

            // Enqueue unvisited pools
            for (ResourceKey<StructureTemplatePool> poolKey : referencedPoolKeys) {
                Identifier poolId = poolKey.identifier();
                if (visitedPools.add(poolId)) {
                    poolRegistry.get(poolId).ifPresent(holder -> queue.add(holder.value()));
                }
            }
        }
    }

    private void collectElementEntities(
        StructurePoolElement element,
        StructureTemplateManager templateManager,
        Identifier structureId,
        Entry<Structure> structureEntry,
        Set<EntityType<?>> seenStructureEntities
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            Identifier templateId = singlePoolElement.getTemplateLocation();
            Optional<StructureTemplate> template = templateManager.get(templateId);
            if (template.isEmpty()) {
                return;
            }
            StructureTemplate tmpl = template.get();
            List<StructureTemplate.StructureEntityInfo> entities =
                ((StructureTemplateIMixin) tmpl).biologydictionary$getEntityInfoList();

            for (StructureTemplate.StructureEntityInfo entityInfo : entities) {
                CompoundTag nbt = entityInfo.nbt;
                if (nbt == null || !nbt.contains("id")) {
                    continue;
                }
                nbt.getString("id").flatMap(EntityType::byString).ifPresent(entityType -> {
                    if (seenStructureEntities.add(entityType)) {
                        spawnStructures.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(structureEntry);
                        structureEntities.computeIfAbsent(structureId, k -> new ArrayList<>())
                            .add(entityType);
                    }
                });
            }
        } else if (element instanceof ListPoolElement listPoolElement) {
            for (StructurePoolElement child : ((ListPoolElementIMixin) listPoolElement).biologydictionary$getElements()) {
                collectElementEntities(child, templateManager, structureId, structureEntry, seenStructureEntities);
            }
        }
    }

    private void collectReferencedPools(
        StructurePoolElement element,
        StructureTemplateManager templateManager,
        Set<ResourceKey<StructureTemplatePool>> referencedPools
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            Identifier templateId = singlePoolElement.getTemplateLocation();
            Optional<StructureTemplate> template = templateManager.get(templateId);
            if (template.isEmpty()) {
                return;
            }
            StructureTemplate tmpl = template.get();
            for (StructureTemplate.Palette palette : ((StructureTemplateIMixin) tmpl).biologydictionary$getPalettes()) {
                for (StructureTemplate.JigsawBlockInfo jigsaw : palette.jigsaws()) {
                    referencedPools.add(jigsaw.pool());
                }
            }
        } else if (element instanceof ListPoolElement listPoolElement) {
            for (StructurePoolElement child : ((ListPoolElementIMixin) listPoolElement).biologydictionary$getElements()) {
                collectReferencedPools(child, templateManager, referencedPools);
            }
        }
    }

    public record Entry<T>(Identifier id, T value) {}
}

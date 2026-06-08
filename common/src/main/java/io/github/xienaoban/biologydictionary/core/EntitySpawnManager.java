package io.github.xienaoban.biologydictionary.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.xienaoban.biologydictionary.mixin.*;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
public final class EntitySpawnManager {
    private static final String SPAWN_OVERRIDE_PATH = "biologydictionary/entity_spawn";
    private static final FileToIdConverter STRUCTURE_LISTER = new FileToIdConverter("structures", ".nbt");
    private static final FileToIdConverter SPAWN_OVERRIDE_LISTER = FileToIdConverter.json(SPAWN_OVERRIDE_PATH);
    private static final CompoundTag MISSING_TEMPLATE = new CompoundTag();
    private static final ResourceLocation IGNORED_MISSING_TEMPLATE = new ResourceLocation(
            "minecraft", "structures/ancient_city/walls/intact_horizontal_wall_stairs_5.nbt"
    );

    private static final String KEY_BIOMES = "biomes";
    private static final String KEY_STRUCTURES = "structures";
    private static final String KEY_OVERWRITE = "overwrite";
    private static final String KEY_ADD = "add";
    private static final String KEY_REMOVE = "remove";

    private RegistryAccess registryAccess;
    private ResourceManager resourceManager;
    private final SpawnMap biomeSpawnMap = new SpawnMap(Registries.BIOME, "biome");
    private final SpawnMap structureSpawnMap = new SpawnMap(Registries.STRUCTURE, "structure");

    public EntitySpawnManager(RegistryAccess registryAccess, ResourceManager resourceManager) {
        this.registryAccess = registryAccess;
        this.resourceManager = resourceManager;
        buildSpawnBiomes();
        buildSpawnStructures();
        applyDataPackOverrides();
        this.registryAccess = null;
        this.resourceManager = null;
    }

    public Set<ResourceLocation> getSpawnBiomes(EntityType<?> entityType) {
        return biomeSpawnMap.getForward(entityType);
    }

    public Set<EntityType<?>> getBiomeEntities(ResourceLocation biomeId) {
        return biomeSpawnMap.getReverse(biomeId);
    }

    public Set<ResourceLocation> getSpawnStructures(EntityType<?> entityType) {
        return structureSpawnMap.getForward(entityType);
    }

    public Set<EntityType<?>> getStructureEntities(ResourceLocation structureId) {
        return structureSpawnMap.getReverse(structureId);
    }


    private void buildSpawnBiomes() {
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : biomeRegistry.entrySet()) {
            try {
                ResourceLocation biomeId = biomeEntry.getKey().location();
                Biome biome = biomeEntry.getValue();
                MobSpawnSettings spawnSettings = biome.getMobSettings();

                Set<EntityType<?>> seenBiomeEntities = new HashSet<>();
                for (MobCategory category : MobCategory.values()) {
                    WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = spawnSettings.getMobs(category);
                    for (MobSpawnSettings.SpawnerData spawnerData : spawners.unwrap()) {
                        EntityType<?> entityType = spawnerData.type;

                        if (seenBiomeEntities.add(entityType)) {
                            biomeSpawnMap.add(entityType, biomeId);
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to process spawn settings for biome {}", biomeEntry.getKey(), e);
            }
        }
    }

    private void buildSpawnStructures() {
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureTemplatePool> poolRegistry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);

        Map<ResourceLocation, CompoundTag> templateCache = new HashMap<>();

        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureRegistry.entrySet()) {
            try {
                ResourceLocation structureId = structureEntry.getKey().location();
                Structure structure = structureEntry.getValue();

                Set<EntityType<?>> seenStructureEntities = new HashSet<>();

                // 1. spawnOverrides (e.g. guardians in ocean monuments)
                structure.spawnOverrides().forEach((category, override) -> {
                    for (MobSpawnSettings.SpawnerData spawnerData : override.spawns().unwrap()) {
                        EntityType<?> entityType = spawnerData.type;

                        if (seenStructureEntities.add(entityType)) {
                            structureSpawnMap.add(entityType, structureId);
                        }
                    }
                });

                // 2. Template entities for Jigsaw structures (e.g. villagers in villages)
                if (structure instanceof JigsawStructure) {
                    Holder<StructureTemplatePool> startPool = ((JigsawStructureIMixin) (Object) structure).biologydictionary$getStartPool();
                    collectTemplateEntities(
                        startPool, poolRegistry, templateCache,
                        structureId, seenStructureEntities
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
        Map<ResourceLocation, CompoundTag> templateCache,
        ResourceLocation structureId,
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
                        element, templateCache,
                        structureId, seenStructureEntities, referencedPools
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
        Map<ResourceLocation, CompoundTag> templateCache,
        ResourceLocation structureId,
        Set<EntityType<?>> seenStructureEntities,
        Set<ResourceLocation> referencedPools
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            ResourceLocation templateId = getTemplateLocation(singlePoolElement);
            if (templateId == null) return;

            CompoundTag rootNbt = templateCache.computeIfAbsent(templateId, id -> {
                ResourceLocation resourceLoc = STRUCTURE_LISTER.idToFile(id);
                try {
                    try (InputStream is = resourceManager.open(resourceLoc)) {
                        return NbtIo.readCompressed(is);
                    }
                } catch (Throwable e) {
                    // Ignore intact_horizontal_wall_stairs_5.nbt
                    if (resourceLoc.equals(IGNORED_MISSING_TEMPLATE) && e instanceof FileNotFoundException) {
                        return MISSING_TEMPLATE;
                    }
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
                        structureSpawnMap.add(entityType, structureId);
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
                collectElementEntities(child, templateCache,
                    structureId, seenStructureEntities, referencedPools);
            }
        }
    }

    private static ResourceLocation getTemplateLocation(SinglePoolElement element) {
        return ((SinglePoolElementIMixin) element).biologydictionary$getTemplate().left().orElse(null);
    }

    // ---- Data Pack Override ----

    private static final String SPAWN_OVERRIDE_PATH_PREFIX = SPAWN_OVERRIDE_PATH + "/";

    private void applyDataPackOverrides() {
        Map<ResourceLocation, List<Resource>> stacks = SPAWN_OVERRIDE_LISTER.listMatchingResourceStacks(resourceManager);
        for (Map.Entry<ResourceLocation, List<Resource>> entry : stacks.entrySet()) {
            String fullPath = entry.getKey().getPath();
            String fileName = fullPath.substring(SPAWN_OVERRIDE_PATH_PREFIX.length());
            String entityStr = fileName.substring(0, fileName.length() - ".json".length());
            int dotIndex = entityStr.indexOf('.');
            if (dotIndex < 0) {
                LOGGER.warn("Invalid spawn override filename '{}', expected format '<namespace>.<entity_path>.json'", fileName);
                continue;
            }
            ResourceLocation entityId = ResourceLocation.tryParse(entityStr.replace('.', ':'));
            if (entityId == null) {
                LOGGER.warn("Invalid entity type '{}' in spawn override data pack, skipping.", entityStr);
                continue;
            }
            EntityType<?> entityType = EntityType.byString(entityId.toString()).orElse(null);
            if (entityType == null) {
                LOGGER.warn("Unknown entity type '{}' in spawn override data pack, skipping.", entityId);
                continue;
            }
            List<Resource> resources = entry.getValue();
            // Traverse from low to high priority.
            for (int i = resources.size() - 1; i >= 0; i--) {
                Resource resource = resources.get(i);
                try (BufferedReader reader = resource.openAsReader()) {
                    JsonObject json = GsonHelper.parse(reader);
                    applyOverrides(entityType, json, KEY_BIOMES, biomeSpawnMap);
                    applyOverrides(entityType, json, KEY_STRUCTURES, structureSpawnMap);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse spawn override for entity '{}': {}", entityId, e);
                }
            }
        }
    }

    private void applyOverrides(EntityType<?> entityType, JsonObject json, String key,
                               SpawnMap spawnMap) {
        if (!json.has(key)) return;
        JsonObject obj = json.getAsJsonObject(key);

        if (obj.has(KEY_OVERWRITE) && (obj.has(KEY_ADD) || obj.has(KEY_REMOVE))) {
            LOGGER.warn("Spawn override for entity '{}' has both 'overwrite' and 'add'/'remove' in {}, using overwrite.", entityType, key);
        }

        if (obj.has(KEY_OVERWRITE)) {
            spawnMap.replace(entityType, obj.getAsJsonArray(KEY_OVERWRITE));
        } else {
            if (obj.has(KEY_ADD)) {
                spawnMap.add(entityType, obj.getAsJsonArray(KEY_ADD));
            }
            if (obj.has(KEY_REMOVE)) {
                spawnMap.remove(entityType, obj.getAsJsonArray(KEY_REMOVE));
            }
        }
    }

    private class SpawnMap {
        private final Map<EntityType<?>, Set<ResourceLocation>> forward = new HashMap<>();
        private final Map<ResourceLocation, Set<EntityType<?>>> reverse = new HashMap<>();
        private final ResourceKey<? extends Registry<?>> registryKey;
        private final String kindName;

        SpawnMap(ResourceKey<? extends Registry<?>> registryKey, String kindName) {
            this.registryKey = registryKey;
            this.kindName = kindName;
        }

        Set<ResourceLocation> getForward(EntityType<?> entityType) {
            return forward.getOrDefault(entityType, Set.of());
        }

        Set<EntityType<?>> getReverse(ResourceLocation id) {
            return reverse.getOrDefault(id, Set.of());
        }

        boolean add(EntityType<?> entityType, ResourceLocation id) {
            boolean added = forward.computeIfAbsent(entityType, k -> new LinkedHashSet<>()).add(id);
            boolean reverseAdded = reverse.computeIfAbsent(id, k -> new LinkedHashSet<>()).add(entityType);
            if (added != reverseAdded) {
                throw new IllegalStateException("Forward/reverse mismatch on add: " + entityType + " <-> " + id);
            }
            return added;
        }

        boolean remove(EntityType<?> entityType, ResourceLocation id) {
            Set<ResourceLocation> entries = forward.get(entityType);
            Set<EntityType<?>> entities = reverse.get(id);
            boolean removed = entries != null && entries.remove(id);
            boolean reverseRemoved = entities != null && entities.remove(entityType);
            if (removed != reverseRemoved) {
                throw new IllegalStateException("Forward/reverse mismatch on remove: " + entityType + " <-> " + id);
            }
            return removed;
        }

        void replace(EntityType<?> entityType, JsonArray array) {
            Set<ResourceLocation> old = forward.get(entityType);
            if (old != null) {
                for (ResourceLocation id : new ArrayList<>(old)) {
                    remove(entityType, id);
                }
            }
            for (ResourceLocation id : parseIdentifierList(array, entityType)) {
                add(entityType, id);
            }
        }

        void add(EntityType<?> entityType, JsonArray array) {
            for (ResourceLocation id : parseIdentifierList(array, entityType)) {
                if (!add(entityType, id)) {
                    LOGGER.warn("{} '{}' already exists for entity '{}', skipping.", kindName, id, entityType);
                }
            }
        }

        void remove(EntityType<?> entityType, JsonArray array) {
            for (ResourceLocation id : parseIdentifierList(array, entityType)) {
                if (!remove(entityType, id)) {
                    LOGGER.warn("{} '{}' does not exist for entity '{}', skipping removal.", kindName, id, entityType);
                }
            }
        }

        private List<ResourceLocation> parseIdentifierList(JsonArray array, EntityType<?> entityType) {
            List<ResourceLocation> result = new ArrayList<>();
            var registry = registryAccess.registryOrThrow(registryKey);
            for (JsonElement element : array) {
                String str = element.getAsString();
                if (str.startsWith("#")) {
                    TagKey<Object> tagKey = TagKey.create(Misc.cast(registryKey), new ResourceLocation(str.substring(1)));
                    var optional = registry.getTag(Misc.cast(tagKey));
                    if (optional.isEmpty()) {
                        LOGGER.warn("Tag '{}' not found in registry, ignoring.", str);
                    } else {
                        optional.get().forEach(holder -> holder.unwrapKey().ifPresent(key -> result.add(key.location())));
                    }
                } else {
                    ResourceLocation id = ResourceLocation.tryParse(str);
                    if (id == null) {
                        LOGGER.warn("Invalid identifier '{}' in spawn override, ignoring.", str);
                    } else if (registry.get(id) == null) {
                        LOGGER.warn("Unknown {} '{}' in spawn override for entity '{}', ignoring.", kindName, id, entityType);
                    } else {
                        result.add(id);
                    }
                }
            }
            return result;
        }
    }
}

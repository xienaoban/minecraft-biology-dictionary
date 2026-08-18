package io.github.xienaoban.biologydictionary.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.xienaoban.biologydictionary.BiologyDictionaryClient;
import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.mixin.JigsawStructureIMixin;
import io.github.xienaoban.biologydictionary.mixin.ListPoolElementIMixin;
import io.github.xienaoban.biologydictionary.mixin.SinglePoolElementIMixin;
import io.github.xienaoban.biologydictionary.mixin.StructureTemplatePoolIMixin;
import io.github.xienaoban.biologydictionary.platform.ClientAndServer;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import java.io.*;
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
    private static final String JIGSAW_BLOCK_ID = "minecraft:jigsaw";
    private static final String SPAWNER_BLOCK_ID = "minecraft:spawner";
    private static final ResourceLocation IGNORED_MISSING_TEMPLATE = IdentifierUtils.mc(
            "structures/ancient_city/walls/intact_horizontal_wall_stairs_5.nbt"
    );
    private static final ResourceLocation EMPTY_POOL = IdentifierUtils.mc("empty");

    private static final String KEY_BIOMES = "biomes";
    private static final String KEY_STRUCTURES = "structures";
    private static final String KEY_OVERWRITE = "overwrite";
    private static final String KEY_ADD = "add";
    private static final String KEY_REMOVE = "remove";

    private BuildContext buildContext;
    private final SpawnMap biomeSpawnMap = new SpawnMap(Registries.BIOME, "biome");
    private final SpawnMap structureSpawnMap = new SpawnMap(Registries.STRUCTURE, "structure");

    public EntitySpawnManager(RegistryAccess registryAccess, ResourceManager resourceManager) {
        int timeoutSeconds = ConfigsManager.getServer().getEntitySpawnAnalysisTimeoutSeconds();
        if (timeoutSeconds == 0) {
            LOGGER.info("Entity spawn analysis is disabled by config. Spawn biome and structure information will be empty.");
            return;
        }

        this.buildContext = new BuildContext(registryAccess, resourceManager, timeoutSeconds);
        try {
            if (!buildContext.timedOut) {
                buildSpawnBiomes();
            }
            if (!buildContext.timedOut) {
                buildSpawnStructures();
            }
            applyDataPackOverrides();
        } finally {
            this.buildContext = null;
        }
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
        Registry<Biome> biomeRegistry = buildContext.registryAccess.registryOrThrow(Registries.BIOME);
        Set<Map.Entry<ResourceKey<Biome>, Biome>> biomeEntries = biomeRegistry.entrySet();
        int totalBiomes = biomeEntries.size();
        buildContext.totalBiomes = totalBiomes;

        long startMillis = System.currentTimeMillis();
        LOGGER.info("Building entity spawn biome map: {} biomes to process.", totalBiomes);
        for (Map.Entry<ResourceKey<Biome>, Biome> biomeEntry : biomeEntries) {
            if (buildContext.isAnalysisTimedOut()) {
                buildContext.markTimedOut();
                break;
            }
            try {
                ResourceLocation biomeId = biomeEntry.getKey().location();
                Biome biome = biomeEntry.getValue();
                MobSpawnSettings spawnSettings = biome.getMobSettings();

                for (MobCategory category : MobCategory.values()) {
                    WeightedRandomList<MobSpawnSettings.SpawnerData> spawners = spawnSettings.getMobs(category);
                    for (MobSpawnSettings.SpawnerData spawnerData : spawners.unwrap()) {
                        biomeSpawnMap.add(spawnerData.type, biomeId);
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to process spawn settings for biome {}", biomeEntry.getKey(), e);
            } finally {
                buildContext.processedBiomes++;
            }
        }
        LOGGER.info("Built entity spawn biome map: processed {}/{} biomes in {} ms.",
            buildContext.processedBiomes, totalBiomes, System.currentTimeMillis() - startMillis);
    }

    private void buildSpawnStructures() {
        Registry<Structure> structureRegistry = buildContext.registryAccess.registryOrThrow(Registries.STRUCTURE);
        Registry<StructureTemplatePool> poolRegistry = buildContext.registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        Set<Map.Entry<ResourceKey<Structure>, Structure>> structureEntries = structureRegistry.entrySet();
        int totalStructures = structureEntries.size();
        buildContext.totalStructures = totalStructures;

        long startMillis = System.currentTimeMillis();
        LOGGER.info("Building entity spawn structure map: {} structures to process.", totalStructures);
        Map<ResourceLocation, ResourceLocation> structureStartPools = new HashMap<>();
        for (Map.Entry<ResourceKey<Structure>, Structure> structureEntry : structureEntries) {
            if (buildContext.isAnalysisTimedOut()) {
                buildContext.markTimedOut();
                break;
            }
            ResourceLocation structureId = structureEntry.getKey().location();
            try {
                Structure structure = structureEntry.getValue();

                // 1. spawnOverrides (e.g. guardians in ocean monuments)
                structure.spawnOverrides().forEach((category, override) -> {
                    for (MobSpawnSettings.SpawnerData spawnerData : override.spawns().unwrap()) {
                        structureSpawnMap.add(spawnerData.type, structureId);
                    }
                });

                // 2. Template entities for Jigsaw structures (e.g. villagers in villages)
                if (structure instanceof JigsawStructure) {
                    Holder<StructureTemplatePool> startPool = ((JigsawStructureIMixin) (Object) structure).biologydictionary$getStartPool();
                    ResourceLocation startPoolId = getPoolLocation(startPool);
                    if (startPoolId != null) {
                        structureStartPools.put(structureId, startPoolId);
                        collectPoolGraph(startPoolId, poolRegistry);
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to process spawn settings for structure {}", structureEntry.getKey(), e);
            } finally {
                buildContext.processedStructures++;
            }
        }
        buildPoolComponents();
        applyStructureTemplateEntities(structureStartPools);
        LOGGER.info("Built entity spawn structure map: processed {}/{} structures in {} ms, cached {} component closures, {} pools, {} templates.",
            buildContext.processedStructures, totalStructures, System.currentTimeMillis() - startMillis,
            buildContext.poolClosureCache.size(), buildContext.poolDirectCache.size(), buildContext.templateCache.size());
        if (!buildContext.missingTemplates.isEmpty()) {
            LOGGER.warn("Skipped {} missing structure templates: {}", buildContext.missingTemplates.size(), buildContext.missingTemplates);
        }
    }

    private void collectPoolGraph(
        ResourceLocation startPoolId,
        Registry<StructureTemplatePool> poolRegistry
    ) {
        Queue<ResourceLocation> poolQueue = new ArrayDeque<>();
        if (buildContext.discoveredPools.add(startPoolId)) {
            poolQueue.add(startPoolId);
        }

        while (!poolQueue.isEmpty()) {
            ResourceLocation currentPoolId = poolQueue.poll();
            StructureAnalysis directAnalysis = analyzePoolDirect(currentPoolId, poolRegistry);

            // Follow jigsaw-referenced pools from templates
            for (ResourceLocation poolId : directAnalysis.referencedPools()) {
                buildContext.poolGraph.computeIfAbsent(currentPoolId, id -> new HashSet<>()).add(poolId);
                if (buildContext.discoveredPools.add(poolId)) {
                    poolQueue.add(poolId);
                }
            }
        }
    }

    private void applyStructureTemplateEntities(Map<ResourceLocation, ResourceLocation> structureStartPools) {
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : structureStartPools.entrySet()) {
            ResourceLocation structureId = entry.getKey();
            ResourceLocation startPoolId = entry.getValue();
            ResourceLocation componentId = buildContext.poolToComponent.get(startPoolId);
            if (componentId == null) continue;
            for (EntityType<?> entityType : analyzeComponentClosure(componentId)) {
                structureSpawnMap.add(entityType, structureId);
            }
        }
    }

    private void buildPoolComponents() {
        for (ResourceLocation poolId : buildContext.discoveredPools) {
            if (!buildContext.tarjanIndices.containsKey(poolId)) {
                strongConnect(poolId);
            }
        }
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : buildContext.poolGraph.entrySet()) {
            ResourceLocation componentId = buildContext.poolToComponent.get(entry.getKey());
            if (componentId == null) continue;
            ComponentAnalysis component = buildContext.components.get(componentId);
            if (component == null) continue;
            for (ResourceLocation referencedPoolId : entry.getValue()) {
                ResourceLocation referencedComponentId = buildContext.poolToComponent.get(referencedPoolId);
                if (referencedComponentId != null && !referencedComponentId.equals(componentId)) {
                    component.referencedComponents().add(referencedComponentId);
                }
            }
        }
    }

    private void strongConnect(ResourceLocation poolId) {
        buildContext.tarjanIndices.put(poolId, buildContext.nextTarjanIndex);
        buildContext.tarjanLowLinks.put(poolId, buildContext.nextTarjanIndex);
        buildContext.nextTarjanIndex++;
        buildContext.tarjanStack.push(poolId);
        buildContext.tarjanStackSet.add(poolId);

        for (ResourceLocation referencedPoolId : buildContext.poolGraph.getOrDefault(poolId, Set.of())) {
            if (!buildContext.tarjanIndices.containsKey(referencedPoolId)) {
                strongConnect(referencedPoolId);
                buildContext.tarjanLowLinks.put(poolId, Math.min(
                    buildContext.tarjanLowLinks.get(poolId),
                    buildContext.tarjanLowLinks.get(referencedPoolId)
                ));
            } else if (buildContext.tarjanStackSet.contains(referencedPoolId)) {
                buildContext.tarjanLowLinks.put(poolId, Math.min(
                    buildContext.tarjanLowLinks.get(poolId),
                    buildContext.tarjanIndices.get(referencedPoolId)
                ));
            }
        }

        if (!buildContext.tarjanLowLinks.get(poolId).equals(buildContext.tarjanIndices.get(poolId))) {
            return;
        }

        Set<ResourceLocation> componentPools = new HashSet<>();
        ResourceLocation componentId = poolId;
        ResourceLocation memberPoolId;
        do {
            memberPoolId = buildContext.tarjanStack.pop();
            buildContext.tarjanStackSet.remove(memberPoolId);
            componentPools.add(memberPoolId);
            if (memberPoolId.toString().compareTo(componentId.toString()) < 0) {
                componentId = memberPoolId;
            }
        } while (!memberPoolId.equals(poolId));

        Set<EntityType<?>> entities = new HashSet<>();
        for (ResourceLocation memberId : componentPools) {
            buildContext.poolToComponent.put(memberId, componentId);
            entities.addAll(buildContext.poolDirectCache.getOrDefault(memberId, StructureAnalysis.EMPTY).entities());
        }

        buildContext.components.put(componentId, new ComponentAnalysis(Set.copyOf(entities), new HashSet<>()));
    }

    private Set<EntityType<?>> analyzeComponentClosure(ResourceLocation componentId) {
        Set<EntityType<?>> cached = buildContext.poolClosureCache.get(componentId);
        if (cached != null) return cached;

        ComponentAnalysis component = buildContext.components.get(componentId);
        if (component == null) return Set.of();

        Set<EntityType<?>> entities = new HashSet<>(component.entities());
        for (ResourceLocation referencedComponentId : component.referencedComponents()) {
            entities.addAll(analyzeComponentClosure(referencedComponentId));
        }

        Set<EntityType<?>> closure = Set.copyOf(entities);
        buildContext.poolClosureCache.put(componentId, closure);
        return closure;
    }

    private StructureAnalysis analyzePoolDirect(
        ResourceLocation poolId,
        Registry<StructureTemplatePool> poolRegistry
    ) {
        StructureAnalysis cached = buildContext.poolDirectCache.get(poolId);
        if (cached != null) return cached;

        Optional<StructureTemplatePool> poolHolder = poolRegistry.getOptional(poolId);
        if (poolHolder.isEmpty()) {
            buildContext.poolDirectCache.put(poolId, StructureAnalysis.EMPTY);
            return StructureAnalysis.EMPTY;
        }

        StructureTemplatePool pool = poolHolder.get();
        Set<EntityType<?>> entities = new HashSet<>();
        Set<ResourceLocation> referencedPools = new HashSet<>();

        // Collect entities from all templates in this pool
        for (StructurePoolElement element : ((StructureTemplatePoolIMixin) pool).biologydictionary$getTemplates()) {
            try {
                StructureAnalysis elementAnalysis = analyzeElement(element);
                entities.addAll(elementAnalysis.entities());
                referencedPools.addAll(elementAnalysis.referencedPools());
            } catch (Exception e) {
                LOGGER.warn("Failed to process element in template pool {}", poolId, e);
            }
        }

        // Also follow fallback pool
        try {
            Holder<StructureTemplatePool> fallback = pool.getFallback();
            if (fallback.value() != pool) {
                ResourceLocation fallbackId = getPoolLocation(fallback);
                if (fallbackId != null) {
                    referencedPools.add(fallbackId);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to process fallback pool for {}", poolId, e);
        }

        StructureAnalysis analysis = new StructureAnalysis(Set.copyOf(entities), Set.copyOf(referencedPools));
        buildContext.poolDirectCache.put(poolId, analysis);
        return analysis;
    }

    private StructureAnalysis analyzeElement(
        StructurePoolElement element
    ) {
        if (element instanceof SinglePoolElement singlePoolElement) {
            ResourceLocation templateId = getTemplateLocation(singlePoolElement);
            if (templateId == null) return StructureAnalysis.EMPTY;
            return analyzeTemplate(templateId);
        } else if (element instanceof ListPoolElement listPoolElement) {
            Set<EntityType<?>> entities = new HashSet<>();
            Set<ResourceLocation> referencedPools = new HashSet<>();
            for (StructurePoolElement child : ((ListPoolElementIMixin) listPoolElement).biologydictionary$getElements()) {
                StructureAnalysis childAnalysis = analyzeElement(child);
                entities.addAll(childAnalysis.entities());
                referencedPools.addAll(childAnalysis.referencedPools());
            }
            return new StructureAnalysis(Set.copyOf(entities), Set.copyOf(referencedPools));
        }
        return StructureAnalysis.EMPTY;
    }

    private StructureAnalysis analyzeTemplate(ResourceLocation templateId) {
        StructureAnalysis cached = buildContext.templateCache.get(templateId);
        if (cached != null) return cached;
        if (templateId.equals(EMPTY_POOL)) {
            buildContext.templateCache.put(templateId, StructureAnalysis.EMPTY);
            return StructureAnalysis.EMPTY;
        }

        ResourceLocation resourceLoc = STRUCTURE_LISTER.idToFile(templateId);
        byte[] templateBytes;
        try (InputStream is = buildContext.resourceManager.open(resourceLoc)) {
            templateBytes = is.readAllBytes();
        } catch (Throwable e) {
            if (e instanceof FileNotFoundException) {
                if (!resourceLoc.equals(IGNORED_MISSING_TEMPLATE)) {
                    buildContext.missingTemplates.add(templateId);
                }
            } else {
                LOGGER.warn("Failed to read structure template resource {}", templateId, e);
            }
            buildContext.templateCache.put(templateId, StructureAnalysis.EMPTY);
            return StructureAnalysis.EMPTY;
        }

        TemplateVisitor visitor;
        try {
            PaletteVisitor paletteVisitor = new PaletteVisitor();
            parseTemplateNbt(templateBytes, paletteVisitor);

            BlockStateVisitor blockStateVisitor = new BlockStateVisitor(paletteVisitor.jigsawStates, paletteVisitor.spawnerStates);
            parseTemplateNbt(templateBytes, blockStateVisitor);

            visitor = new TemplateVisitor(blockStateVisitor.jigsawBlocks, blockStateVisitor.spawnerBlocks);
            parseTemplateNbt(templateBytes, visitor);
            buildContext.parsedTemplates++;
        } catch (Throwable e) {
            LOGGER.warn("Failed to parse structure template {}", templateId, e);
            buildContext.templateCache.put(templateId, StructureAnalysis.EMPTY);
            return StructureAnalysis.EMPTY;
        }
        StructureAnalysis analysis = new StructureAnalysis(
            Set.copyOf(visitor.entities),
            Set.copyOf(visitor.referencedPools)
        );
        buildContext.templateCache.put(templateId, analysis);
        return analysis;
    }

    private static void parseTemplateNbt(byte[] templateBytes, StreamTagVisitor visitor) throws IOException {
        NbtIo.parseCompressed(new ByteArrayInputStream(templateBytes), visitor);
    }

    private static ResourceLocation getTemplateLocation(SinglePoolElement element) {
        return ((SinglePoolElementIMixin) element).biologydictionary$getTemplate().left().orElse(null);
    }

    private static ResourceLocation getPoolLocation(Holder<StructureTemplatePool> pool) {
        return pool.unwrapKey().map(ResourceKey::location).orElse(null);
    }

    private record StructureAnalysis(Set<EntityType<?>> entities, Set<ResourceLocation> referencedPools) {
        private static final StructureAnalysis EMPTY = new StructureAnalysis(Set.of(), Set.of());
    }

    private record ComponentAnalysis(Set<EntityType<?>> entities, Set<ResourceLocation> referencedComponents) {}

    private static final class PaletteVisitor extends AbstractTemplateVisitor {
        private enum Context {
            ROOT,
            PALETTES_LIST,
            PALETTE_LIST,
            PALETTE_ENTRY
        }

        private final Set<Integer> jigsawStates = new HashSet<>();
        private final Set<Integer> spawnerStates = new HashSet<>();
        private final Deque<Context> contextStack = new ArrayDeque<>();
        private int paletteIndex;
        private boolean readingBlockName;

        @Override
        public ValueResult visit(String string) {
            if (readingBlockName) {
                if (JIGSAW_BLOCK_ID.equals(string)) {
                    jigsawStates.add(paletteIndex);
                } else if (SPAWNER_BLOCK_ID.equals(string)) {
                    spawnerStates.add(paletteIndex);
                }
                readingBlockName = false;
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitList(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if (context == Context.PALETTE_LIST && tagType == CompoundTag.TYPE) {
                return ValueResult.CONTINUE;
            }
            if (context == Context.PALETTES_LIST && tagType == ListTag.TYPE) {
                return ValueResult.CONTINUE;
            }
            return ValueResult.BREAK;
        }

        @Override
        public EntryResult visitEntry(TagType<?> tagType, String string) {
            Context context = contextStack.peek();
            if (context == Context.ROOT && tagType == ListTag.TYPE) {
                if ("palette".equals(string)) {
                    contextStack.push(Context.PALETTE_LIST);
                    return EntryResult.ENTER;
                }
                if ("palettes".equals(string)) {
                    contextStack.push(Context.PALETTES_LIST);
                    return EntryResult.ENTER;
                }
            } else if (context == Context.PALETTE_ENTRY && tagType == StringTag.TYPE && "Name".equals(string)) {
                readingBlockName = true;
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public EntryResult visitElement(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if (context == Context.PALETTE_LIST && tagType == CompoundTag.TYPE) {
                paletteIndex = i;
                contextStack.push(Context.PALETTE_ENTRY);
                return EntryResult.ENTER;
            }
            if (context == Context.PALETTES_LIST && tagType == ListTag.TYPE) {
                contextStack.push(Context.PALETTE_LIST);
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public ValueResult visitContainerEnd() {
            if (!contextStack.isEmpty()) {
                contextStack.pop();
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitRootEntry(TagType<?> tagType) {
            if (tagType == CompoundTag.TYPE) {
                contextStack.push(Context.ROOT);
                return ValueResult.CONTINUE;
            }
            return ValueResult.HALT;
        }
    }

    private static final class BlockStateVisitor extends AbstractTemplateVisitor {
        private enum Context {
            ROOT,
            BLOCKS_LIST,
            BLOCK
        }

        private final Set<Integer> jigsawStates;
        private final Set<Integer> spawnerStates;
        private final Set<Integer> jigsawBlocks = new HashSet<>();
        private final Set<Integer> spawnerBlocks = new HashSet<>();
        private final Deque<Context> contextStack = new ArrayDeque<>();
        private int blockIndex;
        private boolean readingBlockState;

        private BlockStateVisitor(Set<Integer> jigsawStates, Set<Integer> spawnerStates) {
            this.jigsawStates = jigsawStates;
            this.spawnerStates = spawnerStates;
        }

        @Override
        public ValueResult visit(int i) {
            if (readingBlockState) {
                if (jigsawStates.contains(i)) {
                    jigsawBlocks.add(blockIndex);
                } else if (spawnerStates.contains(i)) {
                    spawnerBlocks.add(blockIndex);
                }
                readingBlockState = false;
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitList(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if (context == Context.BLOCKS_LIST && tagType == CompoundTag.TYPE) {
                return ValueResult.CONTINUE;
            }
            return ValueResult.BREAK;
        }

        @Override
        public EntryResult visitEntry(TagType<?> tagType, String string) {
            Context context = contextStack.peek();
            if (context == Context.ROOT && tagType == ListTag.TYPE && "blocks".equals(string)) {
                contextStack.push(Context.BLOCKS_LIST);
                return EntryResult.ENTER;
            }
            if (context == Context.BLOCK && tagType == IntTag.TYPE && "state".equals(string)) {
                readingBlockState = true;
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public EntryResult visitElement(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if (context == Context.BLOCKS_LIST && tagType == CompoundTag.TYPE) {
                blockIndex = i;
                contextStack.push(Context.BLOCK);
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public ValueResult visitContainerEnd() {
            if (!contextStack.isEmpty()) {
                contextStack.pop();
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitRootEntry(TagType<?> tagType) {
            if (tagType == CompoundTag.TYPE) {
                contextStack.push(Context.ROOT);
                return ValueResult.CONTINUE;
            }
            return ValueResult.HALT;
        }
    }

    private static final class TemplateVisitor extends AbstractTemplateVisitor {
        private enum Context {
            ROOT,
            ENTITIES_LIST,
            ENTITY,
            ENTITY_NBT,
            BLOCKS_LIST,
            BLOCK,
            TARGET_BLOCK_NBT,
            SPAWN_DATA,
            SPAWN_DATA_ENTITY,
            SPAWN_POTENTIALS_LIST,
            SPAWN_POTENTIAL
        }

        private enum BlockKind {
            NONE,
            JIGSAW,
            SPAWNER
        }

        private final Set<EntityType<?>> entities = new HashSet<>();
        private final Set<ResourceLocation> referencedPools = new HashSet<>();
        private final Set<Integer> jigsawBlocks;
        private final Set<Integer> spawnerBlocks;
        private final Deque<Context> contextStack = new ArrayDeque<>();
        private boolean readingEntityId;
        private boolean readingPool;
        private BlockKind blockKind = BlockKind.NONE;

        private TemplateVisitor(Set<Integer> jigsawBlocks, Set<Integer> spawnerBlocks) {
            this.jigsawBlocks = jigsawBlocks;
            this.spawnerBlocks = spawnerBlocks;
        }

        @Override
        public ValueResult visit(String string) {
            if (readingEntityId) {
                EntityType.byString(string).ifPresent(entities::add);
                readingEntityId = false;
            } else if (readingPool) {
                ResourceLocation poolLoc = IdentifierUtils.fromStringOrNull(string);
                if (poolLoc != null && !poolLoc.equals(EMPTY_POOL)) {
                    referencedPools.add(poolLoc);
                }
                readingPool = false;
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitList(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if ((context == Context.ENTITIES_LIST || context == Context.BLOCKS_LIST || context == Context.SPAWN_POTENTIALS_LIST)
                && tagType == CompoundTag.TYPE) {
                return ValueResult.CONTINUE;
            }
            return ValueResult.BREAK;
        }

        @Override
        public EntryResult visitEntry(TagType<?> tagType, String string) {
            Context context = contextStack.peek();
            if (context == Context.ROOT && tagType == ListTag.TYPE) {
                if ("entities".equals(string)) {
                    contextStack.push(Context.ENTITIES_LIST);
                    return EntryResult.ENTER;
                }
                if ("blocks".equals(string)) {
                    contextStack.push(Context.BLOCKS_LIST);
                    return EntryResult.ENTER;
                }
            } else if (context == Context.ENTITY && tagType == CompoundTag.TYPE && "nbt".equals(string)) {
                contextStack.push(Context.ENTITY_NBT);
                return EntryResult.ENTER;
            } else if (context == Context.ENTITY_NBT && tagType == StringTag.TYPE && "id".equals(string)) {
                readingEntityId = true;
                return EntryResult.ENTER;
            } else if (context == Context.BLOCK && tagType == CompoundTag.TYPE && "nbt".equals(string)) {
                if (blockKind != BlockKind.NONE) {
                    contextStack.push(Context.TARGET_BLOCK_NBT);
                    return EntryResult.ENTER;
                }
            } else if (context == Context.TARGET_BLOCK_NBT) {
                if (blockKind == BlockKind.JIGSAW && tagType == StringTag.TYPE && "pool".equals(string)) {
                    readingPool = true;
                    return EntryResult.ENTER;
                }
                if (blockKind == BlockKind.SPAWNER && tagType == CompoundTag.TYPE && "SpawnData".equals(string)) {
                    contextStack.push(Context.SPAWN_DATA);
                    return EntryResult.ENTER;
                }
                if (blockKind == BlockKind.SPAWNER && tagType == ListTag.TYPE && "SpawnPotentials".equals(string)) {
                    contextStack.push(Context.SPAWN_POTENTIALS_LIST);
                    return EntryResult.ENTER;
                }
            } else if (context == Context.SPAWN_DATA && tagType == CompoundTag.TYPE && "entity".equals(string)) {
                contextStack.push(Context.SPAWN_DATA_ENTITY);
                return EntryResult.ENTER;
            } else if (context == Context.SPAWN_DATA_ENTITY && tagType == StringTag.TYPE && "id".equals(string)) {
                readingEntityId = true;
                return EntryResult.ENTER;
            } else if (context == Context.SPAWN_POTENTIAL && tagType == CompoundTag.TYPE && "data".equals(string)) {
                contextStack.push(Context.SPAWN_DATA);
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public EntryResult visitElement(TagType<?> tagType, int i) {
            Context context = contextStack.peek();
            if (context == Context.ENTITIES_LIST && tagType == CompoundTag.TYPE) {
                contextStack.push(Context.ENTITY);
                return EntryResult.ENTER;
            }
            if (context == Context.BLOCKS_LIST && tagType == CompoundTag.TYPE) {
                if (jigsawBlocks.contains(i)) {
                    blockKind = BlockKind.JIGSAW;
                } else if (spawnerBlocks.contains(i)) {
                    blockKind = BlockKind.SPAWNER;
                } else {
                    return EntryResult.SKIP;
                }
                contextStack.push(Context.BLOCK);
                return EntryResult.ENTER;
            }
            if (context == Context.SPAWN_POTENTIALS_LIST && tagType == CompoundTag.TYPE) {
                contextStack.push(Context.SPAWN_POTENTIAL);
                return EntryResult.ENTER;
            }
            return EntryResult.SKIP;
        }

        @Override
        public ValueResult visitContainerEnd() {
            if (!contextStack.isEmpty() && contextStack.pop() == Context.BLOCK) {
                blockKind = BlockKind.NONE;
            }
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visitRootEntry(TagType<?> tagType) {
            if (tagType == CompoundTag.TYPE) {
                contextStack.push(Context.ROOT);
                return ValueResult.CONTINUE;
            }
            return ValueResult.HALT;
        }
    }

    // ---- Data Pack Override ----

    private static final String SPAWN_OVERRIDE_PATH_PREFIX = SPAWN_OVERRIDE_PATH + "/";

    private void applyDataPackOverrides() {
        Map<ResourceLocation, List<Resource>> stacks = SPAWN_OVERRIDE_LISTER.listMatchingResourceStacks(buildContext.resourceManager);
        for (Map.Entry<ResourceLocation, List<Resource>> entry : stacks.entrySet()) {
            String fullPath = entry.getKey().getPath();
            String fileName = fullPath.substring(SPAWN_OVERRIDE_PATH_PREFIX.length());
            String entityStr = fileName.substring(0, fileName.length() - ".json".length());
            int dotIndex = entityStr.indexOf('.');
            if (dotIndex < 0) {
                LOGGER.warn("Invalid spawn override filename '{}', expected format '<namespace>.<entity_path>.json'", fileName);
                continue;
            }
            ResourceLocation entityId = IdentifierUtils.fromStringOrNull(entityStr.replace('.', ':'));
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
            var registry = buildContext.registryAccess.registryOrThrow(registryKey);
            for (JsonElement element : array) {
                String str = element.getAsString();
                if (str.startsWith("#")) {
                    TagKey<Object> tagKey = TagKey.create(Misc.cast(registryKey), IdentifierUtils.fromString(str.substring(1)));
                    var optional = registry.getTag(Misc.cast(tagKey));
                    if (optional.isEmpty()) {
                        LOGGER.warn("Tag '{}' not found in registry, ignoring.", str);
                    } else {
                        optional.get().forEach(holder -> holder.unwrapKey().ifPresent(key -> result.add(key.location())));
                    }
                } else {
                    ResourceLocation id = IdentifierUtils.fromStringOrNull(str);
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

    private static final class BuildContext {
        private final RegistryAccess registryAccess;
        private final ResourceManager resourceManager;
        private final long analysisStartMillis = System.currentTimeMillis();
        private final long timeoutMillis;
        // Component id -> all reachable entities from its component graph.
        private final Map<ResourceLocation, Set<EntityType<?>>> poolClosureCache = new HashMap<>();
        // Pool -> directly contained entities and directly referenced pools.
        private final Map<ResourceLocation, StructureAnalysis> poolDirectCache = new HashMap<>();
        // Structure template NBT id -> contained entities and jigsaw-referenced pools.
        private final Map<ResourceLocation, StructureAnalysis> templateCache = new HashMap<>();
        // Pool -> directly referenced pools. This graph is collapsed into SCC components before closure analysis.
        private final Map<ResourceLocation, Set<ResourceLocation>> poolGraph = new HashMap<>();
        private final Set<ResourceLocation> discoveredPools = new HashSet<>();
        private final Map<ResourceLocation, ResourceLocation> poolToComponent = new HashMap<>();
        private final Map<ResourceLocation, ComponentAnalysis> components = new HashMap<>();
        private final Set<ResourceLocation> missingTemplates = new LinkedHashSet<>();
        private final Map<ResourceLocation, Integer> tarjanIndices = new HashMap<>();
        private final Map<ResourceLocation, Integer> tarjanLowLinks = new HashMap<>();
        private final Deque<ResourceLocation> tarjanStack = new ArrayDeque<>();
        private final Set<ResourceLocation> tarjanStackSet = new HashSet<>();
        private boolean timedOut;
        private int totalBiomes;
        private int totalStructures;
        private int processedBiomes;
        private int processedStructures;
        private int parsedTemplates;
        private int nextTarjanIndex;

        private BuildContext(RegistryAccess registryAccess, ResourceManager resourceManager, int timeoutSeconds) {
            this.registryAccess = registryAccess;
            this.resourceManager = resourceManager;
            this.timeoutMillis = timeoutSeconds * 1_000L;
        }

        private boolean isAnalysisTimedOut() {
            return System.currentTimeMillis() - analysisStartMillis >= timeoutMillis;
        }

        private void markTimedOut() {
            if (timedOut) return;
            timedOut = true;
            MutableComponent message = createTimeoutWarningMessage();
            LOGGER.warn("{}", message.getString());
            if (DevUtils.isClient()) {
                sendClientAnalysisWarning(message.withStyle(ChatFormatting.YELLOW));
            }
        }

        private MutableComponent createTimeoutWarningMessage() {
            long elapsedMillis = System.currentTimeMillis() - analysisStartMillis;
            return TextUtils.translate(
                Lang.WARN_SPAWN_ANALYSIS_TIMED_OUT,
                formatDuration(elapsedMillis),
                processedBiomes,
                totalBiomes,
                processedStructures,
                totalStructures,
                parsedTemplates,
                TextUtils.translate(Lang.CONFIG_ENTRY_PREFIX + "entitySpawnAnalysisTimeoutSeconds")
            );
        }

        private static String formatDuration(long millis) {
            if (millis < 1_000L) {
                return millis + " ms";
            }
            return String.format(Locale.ROOT, "%.1f s", millis / 1_000.0);
        }

        @ClientAndServer
        private static void sendClientAnalysisWarning(Component message) {
            @ClientOnly final class CO { static void send(Component message) {
                BiologyDictionaryClient.printLogToTextBoxWhenReady(message);
            }}
            CO.send(message);
        }
    }

    private abstract static class AbstractTemplateVisitor implements StreamTagVisitor {
        @Override
        public ValueResult visitEnd() {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(String string) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(byte b) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(short s) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(int i) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(long l) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(float f) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(double d) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(byte[] bs) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(int[] is) {
            return ValueResult.CONTINUE;
        }

        @Override
        public ValueResult visit(long[] ls) {
            return ValueResult.CONTINUE;
        }

        @Override
        public EntryResult visitEntry(TagType<?> tagType) {
            return EntryResult.ENTER;
        }
    }
}

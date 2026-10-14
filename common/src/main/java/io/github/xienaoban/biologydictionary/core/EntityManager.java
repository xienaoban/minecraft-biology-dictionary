package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class EntityManager {
    /**
     * Get my preferred order of the vanilla entity.
     * Returns Integer rather than int because it can be null.
     */
    public static Integer getMyPreferredEntityOrder(EntityType<?> clazz) {
        return EntityOrder.map.get(clazz);
    }

    public static boolean isEntityTypeBlacklisted(EntityType<?> entityType) {
        return ConfigsManager.getServer().isEntityTypeBlacklisted(EntityUtils.getEntityTypeIdName(entityType));
    }

    private final Map<Class<? extends Entity>, EntityTreeNode> tree = new HashMap<>();
    private final Map<EntityType<?>, EntityDictionaryEntry> entries = new HashMap<>();
    private final List<EntityDictionaryEntry> sortedEntries = new ArrayList<>();
    private final Map<Class<? extends Entity>, EntityType<?>> clazzToType = new HashMap<>();

    public EntityManager(Level level) {
        initEntities(level);
        initEntitiesSortClassInfo();
        initEntitiesSortTreeNode();
    }

    private void initEntities(Level level) {
        tree.put(Entity.class, new EntityTreeNode());
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (!entityType.isEnabled(level.enabledFeatures())) { continue; }

            EntityDictionaryEntry entry;
            try {
                Entity entity = entityType.create(level, EntitySpawnReason.LOAD);
                if (entity == null) {
                    throw new IllegalStateException("Entity type returned null from create().");
                }
                if (!(entity instanceof LivingEntity)) { continue; }
                entry = new EntityDictionaryEntry(entityType, entity.getClass());
            } catch (Throwable e) {
                if (entityType == EntityTypes.PLAYER) {
                    entry = new EntityDictionaryEntry(entityType, Player.class);
                    entry.instanceCreationFailed = true; // no print
                } else {
                    entry = new EntityDictionaryEntry(entityType, null);
                    entry.markInstanceCreationFailed(e);
                }
            }
            entries.put(entityType, entry);
            sortedEntries.add(entry);
            entry.getClazz().ifPresent(clazz -> {
                clazzToType.put(clazz, entityType);
                getOrCreateEntityTreeNode(clazz);
            });
        }
    }

    private void initEntitiesSortClassInfo() {
        sortedEntries.sort((a, b) -> {
            Integer oa = getMyPreferredEntityOrder(a.getType());
            Integer ob = getMyPreferredEntityOrder(b.getType());
            if (oa != null && ob != null) {
                return Integer.compare(oa, ob);
            }
            else if (oa != null || ob != null) {
                return oa == null ? 1 : -1;
            }
            Identifier ia = a.getId();
            Identifier ib = b.getId();
            int cmp = ia.getNamespace().compareTo(ib.getNamespace());
            if (cmp != 0) {
                if (IdentifierUtils.isMc(ia)) {
                    return 1;
                }
                if (IdentifierUtils.isMc(ib)) {
                    return -1;
                }
                return cmp;
            }
            int failedCmp = Boolean.compare(a.isInstanceCreationFailed(), b.isInstanceCreationFailed());
            if (failedCmp != 0) { return failedCmp; }
            String pa = ia.getPath(), pb = ib.getPath();
            return new StringBuilder(pa).reverse().compareTo(new StringBuilder(pb).reverse());
        });
        for (int i = sortedEntries.size() - 1; i >= 0; --i) {
            sortedEntries.get(i).setSortId(i);
        }
    }

    private void initEntitiesSortTreeNode() {
        for (EntityTreeNode node : tree.values()) {
            node.sortSons();
        }
    }

    private EntityTreeNode getOrCreateEntityTreeNode(Class<? extends Entity> clazz) {
        EntityTreeNode node = tree.get(clazz);
        if (node == null) {
            node = new EntityTreeNode(clazz, getOrCreateEntityTreeNode(clazz.getSuperclass().asSubclass(Entity.class)));
            tree.put(clazz, node);
        }
        return node;
    }

    public EntityType<?> getEntityType(Class<? extends Entity> entityClazz) {
        return clazzToType.get(entityClazz);
    }

    public EntityDictionaryEntry getEntityEntry(EntityType<?> entityType) {
        return isEntityTypeBlacklisted(entityType) ? null : getRawEntityEntry(entityType);
    }

    public EntityDictionaryEntry getEntityEntry(Class<? extends Entity> entityClazz) {
        return getEntityEntry(getEntityType(entityClazz));
    }

    public List<EntityDictionaryEntry> getEntityEntries() {
        return sortedEntries.stream().filter(entry -> !isEntityTypeBlacklisted(entry.getType())).toList();
    }

    /**
     * The entries in the dictionary order, blacklist included. The category groups store
     * the raw membership and filter the blacklist when they are read.
     */
    List<EntityDictionaryEntry> getRawEntityEntries() {
        return sortedEntries;
    }

    EntityDictionaryEntry getRawEntityEntry(EntityType<?> entityType) {
        return entries.get(entityType);
    }

    public boolean isVanillaEntity(EntityType<?> entityType) {
        return IdentifierUtils.isMc(EntityUtils.getEntityTypeId(entityType));
    }

    /**
     * Get Deobfuscated class name of vanilla entity-related classes/interfaces.
     */
    public static String getClassRealName(Class<?> clazz) {
        String res = EntityUtils.getDeobfuscatedName(clazz);
        if (res == null) { res = clazz.getName(); }
        return res;
    }

    public void dfsEntityTree(boolean includeRoot, TreeNodeExecutor<EntityTreeNode> executor) {
        dfsEntityTree(includeRoot, executor, TreeNodeExecutor.empty());
    }

    public void dfsEntityTree(boolean includeRoot, TreeNodeExecutor<EntityTreeNode> frontExecutor,
                              TreeNodeExecutor<EntityTreeNode> rearExecutor) {
        EntityTreeNode root = tree.get(Entity.class);
        if (includeRoot) {
            dfsEntityTreePrivate(root, 0, frontExecutor, rearExecutor);
        }
        else {
            for (var son : root.getSons()) {
                dfsEntityTreePrivate(son, 1, frontExecutor, rearExecutor);
            }
        }
    }

    private void dfsEntityTreePrivate(EntityTreeNode root, int depth, TreeNodeExecutor<EntityTreeNode> frontExecutor,
                                      TreeNodeExecutor<EntityTreeNode> rearExecutor) {
        if (frontExecutor.execute(root, depth)) {
            int d2 = depth + 1;
            for (var son : root.getSons()) {
                dfsEntityTreePrivate(son, d2, frontExecutor, rearExecutor);
            }
        }
        rearExecutor.execute(root, depth);
    }

    public static class EntityDictionaryEntry implements Comparable<EntityDictionaryEntry> {
        private final EntityType<?> type;
        private final Class<? extends Entity> clazz;
        private int sortId;
        private boolean instanceCreationFailed;

        private EntityDictionaryEntry(EntityType<?> type, Class<? extends Entity> clazz) {
            this.type = type;
            this.clazz = clazz;
        }

        public EntityType<?> getType() { return type; }
        public Optional<Class<? extends Entity>> getClazz() { return Optional.ofNullable(clazz); }
        public Identifier getId() { return EntityUtils.getEntityTypeId(getType()); }
        public String getStringId() { return IdentifierUtils.toString(getId()); }

        public int getSortId() { return sortId; }
        public void setSortId(int sortId) { this.sortId = sortId; }
        public boolean isInstanceCreationFailed() { return instanceCreationFailed; }
        public void markInstanceCreationFailed(Throwable e) {
            if (instanceCreationFailed) { return; }
            instanceCreationFailed = true;
            LOGGER.error("Failed to create entity type \"{}\".", EntityUtils.getEntityTypeName(type), e);
        }

        @Override
        public String toString() { return type.toString(); }

        @Override
        public int compareTo(EntityDictionaryEntry o) { return Integer.compare(sortId, o.sortId); }
    }

    public static class EntityTreeNode {
        private final Class<? extends Entity> clazz;
        private final EntityTreeNode father;
        private final List<EntityTreeNode> sons;

        public EntityTreeNode() {
            clazz = Entity.class;
            father = null;
            sons = new ArrayList<>();
        }

        public EntityTreeNode(Class<? extends Entity> entityClazz, EntityTreeNode entityFatherNode) {
            clazz = entityClazz;
            father = entityFatherNode;
            sons = new ArrayList<>();
            entityFatherNode.addSon(this);
        }

        public Class<? extends Entity> getClazz() { return clazz; }
        public String getClazzName() { return getClassRealName(clazz); }

        public EntityTreeNode getFather() { return father; }
        public List<EntityTreeNode> getSons() { return sons; }
        protected void addSon(EntityTreeNode son) { sons.add(son); }
        protected void sortSons() {
            sons.sort(Comparator.comparing(EntityTreeNode::getClazzName));
        }

        @Override
        public String toString() {
            return clazz.getSimpleName();
        }
    }
}

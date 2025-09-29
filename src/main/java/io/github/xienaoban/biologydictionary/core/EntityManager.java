package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.common.util.DevUtils;
import io.github.xienaoban.biologydictionary.common.util.EntityUtils;
import io.github.xienaoban.biologydictionary.common.util.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

import static io.github.xienaoban.biologydictionary.BiologyDictionary.BD;
import static io.github.xienaoban.biologydictionary.BiologyDictionary.LOGGER;

public final class EntityManager {
    private static EntityManager instance = null;

    /**
     * Don't invoke it before joining a world because we need a minecraft level.
     */
    public static EntityManager getInstance() {
        return instance;
    }

    public static void init() {
        synchronized (EntityManager.class) {
            if (instance == null) {
                try {
                    Level level = BD.justGiveMeALevel();
                    if (level != null) {
                        instance = new EntityManager(BD.justGiveMeALevel());
                        LOGGER.info("EntityManager initialized.");
                    } else {
                        LOGGER.info("EntityManager not initialized.");
                    }
                } catch (Throwable e) {
                    instance = null;
                    LOGGER.error("Failed to init EntityManager: {}", Misc.getStackToString(e));
                }
            }
        }
    }

    public static void destroy() {
        synchronized (EntityManager.class) {
            if (instance != null) {
                instance = null;
                LOGGER.info("EntityManager destroyed.");
            } else {
                LOGGER.info("EntityManager has been destroyed.");
            }
        }
    }

    /**
     * Get my preferred order of the vanilla entity.
     * Returns Integer rather than int because it can be null.
     */
    public static Integer getMyPreferredEntityOrder(EntityType<?> clazz) {
        return EntityOrder.map.get(clazz);
    }

    private final Map<Class<? extends Entity>, EntityTreeNode> tree = new HashMap<>();
    private final Map<EntityType<?>, EntityClassInfo> infos = new HashMap<>();
    private final List<EntityClassInfo> sortedInfos = new ArrayList<>();
    private final Map<Class<? extends Entity>, EntityType<?>> clazzToType = new HashMap<>();

    private final TagGroup defaultTags   = new TagGroup(Lang.TAG_GROUP_DEFAULT,   Component.translatable(Lang.TAG_GROUP_DEFAULT_DESC));
    private final TagGroup mcTagTags     = new TagGroup(Lang.TAG_GROUP_TAG,       Component.translatable(Lang.TAG_GROUP_TAG_DESC));
    private final TagGroup namespaceTags = new TagGroup(Lang.TAG_GROUP_MODS,      Component.translatable(Lang.TAG_GROUP_MODS_DESC));
    private final TagGroup classTags     = new TagGroup(Lang.TAG_GROUP_CLASS,     Component.translatable(Lang.TAG_GROUP_CLASS_DESC));
    private final TagGroup interfaceTags = new TagGroup(Lang.TAG_GROUP_INTERFACE, Component.translatable(Lang.TAG_GROUP_INTERFACE_DESC));

    private final List<TagGroup> tagGroups = new ArrayList<>(List.of(defaultTags, mcTagTags, namespaceTags, classTags, interfaceTags));

    private EntityManager(Level level) {
        EntityOrder.map.get(null);

        initEntities(level);
        initEntitiesSortClassInfo();
        initEntitiesSortTreeNode();
        initMcTagTagGroups();
        initJavaTagGroups();
        initDefaultTags();
    }

    private void initEntities(Level level) {
        tree.put(Entity.class, new EntityTreeNode());
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            EntityClassInfo entityClassInfo;
            try {
                Optional<EntityClassInfo> o = EntityClassInfo.create(entityType, level);
                if (o.isEmpty()) continue;
                entityClassInfo = o.get();
            } catch (Exception e) {
                LOGGER.error("Cannot init EntityClassInfo of\"" + EntityType.getKey(entityType) + "\": " + e);
                throw e;
            }
            infos.put(entityClassInfo.getType(), entityClassInfo);
            sortedInfos.add(entityClassInfo);
            clazzToType.put(entityClassInfo.getClazz(), entityType);
            getOrCreateEntityTreeNode(entityClassInfo.getClazz());
        }
        // [Should I?] info.put(EntityType.PLAYER, new EntityClassInfo(EntityType.PLAYER, PlayerEntity.class));
        // [Should I?] getEntityTreeNode(PlayerEntity.class);
    }

    private void initEntitiesSortClassInfo() {
        sortedInfos.sort((a, b) -> {
            Integer oa = getMyPreferredEntityOrder(a.getType());
            Integer ob = getMyPreferredEntityOrder(b.getType());
            if (oa != null && ob != null) {
                return oa - ob;
            }
            else if (oa != null || ob != null) {
                return oa == null ? 1 : -1;
            }
            ResourceLocation la = a.getLocation();
            ResourceLocation lb = b.getLocation();
            int cmp = la.getNamespace().compareTo(lb.getNamespace());
            if (cmp != 0) {
                if (la.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                    return 1;
                }
                return cmp;
            }
            String pa = la.getPath(), pb = lb.getPath();
            int i = pa.length() - 1, j = pb.length() - 1;
            while (i >= 0 && j >= 0) {
                cmp = pa.charAt(i) - pb.charAt(j);
                if (cmp != 0) {
                    return cmp;
                }
                --i; --j;
            }
            return i - j;
        });
        for (int i = sortedInfos.size() - 1; i >= 0; --i) {
            sortedInfos.get(i).setSortId(i);
        }
    }

    private void initEntitiesSortTreeNode() {
        for (EntityTreeNode node : tree.values()) {
            node.sortSons();
        }
    }

    private void initMcTagTagGroups() {
        TagGroup tags = mcTagTags;
        BuiltInRegistries.ENTITY_TYPE.getTags()
                .sorted(DevUtils.getResourceLocationComparator(holders -> holders.key().location()))
                .forEach(holders -> {
                    String key = holders.key().location().toLanguageKey();
                    List<EntityClassInfo> list = holders.stream()
                            .map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get)
                            .map(EntityUtils::getEntityType).map(this::getEntityClassInfo).filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(EntityClassInfo::getSortId))
                            .toList();
                    tags.addTag(new Tag(key, null, Component.literal(holders.key().location().toString())));
                    tags.addAllToTag(key, list);
        });
    }

    private void initJavaTagGroups() {
        String rootClazzName = getClassRealName(Entity.class);
        classTags.addTag(new Tag(rootClazzName, null, Component.literal(rootClazzName)));
        dfsEntityTree(false, (root, depth) -> {
            if (!Modifier.isAbstract(root.getClazz().getModifiers())) {
                return false;
            }
            Tag father = classTags.getTag(getClassRealName(root.getFather().getClazz()));
            String clazzName = getClassRealName(root.getClazz());
            classTags.addTag(new Tag(clazzName, father, Component.literal(clazzName)));
            return true;
        });
        for (EntityClassInfo info : sortedInfos) {
            String namespace = EntityUtils.getEntityTypeId(info.getType()).getNamespace();
            namespaceTags.getOrAddTag(namespace,
                    s -> new Tag(s, null, Component.literal(s)));
            namespaceTags.addToTag(namespace, info);

            for (Class<? extends Entity> clazz : EntityUtils.bottomUp(info.getClazz())) {
                String realName = getClassRealName(clazz);
                if (classTags.containsTag(realName)) {
                    classTags.addToTag(realName, info);
                }
                for (Class<?> clazz2 : clazz.getInterfaces()) {
                    if (!clazz2.getSimpleName().contains("Mixin")) {
                        String interfazeName = getClassRealName(clazz2);
                        interfaceTags.getOrAddTag(interfazeName,
                                s -> new Tag(s, null, Component.literal(s)));
                        interfaceTags.addToTag(interfazeName, info);
                    }
                }
            }
        }
        interfaceTags.getRootTags().sort(DevUtils.getClassNameComparator(Tag::getName));
    }

    private void initDefaultTags() {
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_TERRESTRIAL, defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_HUMANOID,    defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY_TERRESTRIAL)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_AQUATIC,     defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_BUCKETABLE,  defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY_AQUATIC)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_FLYING,      defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_FRIENDLY_NEUTRAL,     defaultTags.getTag(Lang.TAG_DEFAULT_FRIENDLY)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_ENEMY));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_ENEMY_HUMANOID,       defaultTags.getTag(Lang.TAG_DEFAULT_ENEMY)));
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_ENEMY_PATROL,         defaultTags.getTag(Lang.TAG_DEFAULT_ENEMY)));

        List<EntityClassInfo> friendlyList = new ArrayList<>();
        List<EntityClassInfo> terrestrialList = new ArrayList<>();
        List<EntityClassInfo> humanList = new ArrayList<>();
        List<EntityClassInfo> aquaticList = new ArrayList<>();
        List<EntityClassInfo> bucketableList = new ArrayList<>();
        List<EntityClassInfo> flyingList = new ArrayList<>();
        List<EntityClassInfo> neutralList = new ArrayList<>();
        List<EntityClassInfo> enemyList = new ArrayList<>();
        List<EntityClassInfo> humanoidList = new ArrayList<>();
        List<EntityClassInfo> patrolList = new ArrayList<>();

        for (EntityClassInfo info : sortedInfos) {
            Class<? extends Entity> entityClazz = info.getClazz();
            Vec3 box = info.getBox();
            boolean ratio = box.y() / box.x() >= 2;
            if (Enemy.class.isAssignableFrom(entityClazz)) {
                enemyList.add(info);
                if (ratio) {
                    humanoidList.add(info);
                }

                if (PatrollingMonster.class.isAssignableFrom(entityClazz)) {
                    patrolList.add(info);
                }
            } else {
                friendlyList.add(info);
                if (ratio) {
                    humanList.add(info);
                }

                if (NeutralMob.class.isAssignableFrom(entityClazz)) {
                    neutralList.add(info);
                }

                if (WaterAnimal.class.isAssignableFrom(entityClazz)
                        || AgeableWaterCreature.class.isAssignableFrom(entityClazz)) {
                    aquaticList.add(info);
                } else if (FlyingAnimal.class.isAssignableFrom(entityClazz)
                        || entityClazz == Bat.class || entityClazz == Allay.class) {
                    flyingList.add(info);
                } else {
                    terrestrialList.add(info);
                }

                if (Bucketable.class.isAssignableFrom(entityClazz)) {
                    bucketableList.add(info);
                }
            }
        }
        humanList.add(getEntityClassInfo(EntityType.IRON_GOLEM));
        aquaticList.add(getEntityClassInfo(EntityType.TURTLE));
        aquaticList.add(getEntityClassInfo(EntityType.AXOLOTL));
        aquaticList.add(getEntityClassInfo(EntityType.FROG));
        humanList.sort(Comparator.comparingInt(EntityClassInfo::getSortId));
        aquaticList.sort(Comparator.comparingInt(EntityClassInfo::getSortId));

        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY, friendlyList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_TERRESTRIAL, terrestrialList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_HUMANOID, humanList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_AQUATIC, aquaticList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_BUCKETABLE, bucketableList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_FLYING, flyingList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_FRIENDLY_NEUTRAL, neutralList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_ENEMY, enemyList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_ENEMY_HUMANOID, humanoidList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_ENEMY_PATROL, patrolList);
    }

    private EntityTreeNode getOrCreateEntityTreeNode(Class<? extends Entity> clazz) {
        EntityTreeNode node = tree.get(clazz);
        if (node == null) {
            node = new EntityTreeNode(clazz, getOrCreateEntityTreeNode(clazz.getSuperclass().asSubclass(Entity.class)));
            tree.put(clazz, node);
        }
        return node;
    }

    public List<TagGroup> getTagGroups() {
        return tagGroups;
    }

    public EntityType<?> getEntityType(Class<? extends Entity> entityClazz) {
        return clazzToType.get(entityClazz);
    }

    public EntityClassInfo getEntityClassInfo(EntityType<?> entityType) {
        return infos.get(entityType);
    }

    public EntityClassInfo getEntityClassInfo(Class<? extends Entity> entityClazz) {
        return getEntityClassInfo(getEntityType(entityClazz));
    }

    public List<EntityClassInfo> getEntityClassInfos() {
        return sortedInfos;
    }

    public boolean isVanillaEntity(EntityType<?> entityType) {
        return ResourceLocation.DEFAULT_NAMESPACE
                .equals(getEntityClassInfo(entityType).getLocation().getNamespace());
    }

    /**
     * Get Deobfuscated class name of vanilla entity-related classes/interfaces.
     */
    public static String getClassRealName(Class<?> clazz) {
        String res = EntityUtils.getDeobfuscatedName(clazz);
        if (res == null) res = clazz.getName();
        return res;
    }

    public void dfsEntityTree(boolean includeRoot, TreeNodeExecutor<EntityTreeNode> executor) {
        dfsEntityTree(includeRoot, executor, TreeNodeExecutor.empty());
    }

    public void dfsEntityTree(boolean includeRoot, TreeNodeExecutor<EntityTreeNode> frontExecutor, TreeNodeExecutor<EntityTreeNode> rearExecutor) {
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

    private void dfsEntityTreePrivate(EntityTreeNode root, int depth, TreeNodeExecutor<EntityTreeNode> frontExecutor, TreeNodeExecutor<EntityTreeNode> rearExecutor) {
        if (frontExecutor.execute(root, depth)) {
            int d2 = depth + 1;
            for (var son : root.getSons()) {
                dfsEntityTreePrivate(son, d2, frontExecutor, rearExecutor);
            }
        }
        rearExecutor.execute(root, depth);
    }

    public static class EntityClassInfo implements Comparable<EntityClassInfo> {
        public static Optional<EntityClassInfo> create(EntityType<?> entityType, Level level) {
            Entity entity = EntityUtils.create(entityType, level);
            if (entity == null) {
                if (entityType == EntityType.PLAYER) return Optional.empty();
                if (!entityType.isEnabled(level.enabledFeatures())) return Optional.empty();
                String name = EntityType.getKey(entityType).toString();
                throw new NullPointerException("Failed to create \"" + name + "\".");
            } else if (!(entity instanceof LivingEntity)) {
                return Optional.empty();
            }
            return Optional.of(new EntityClassInfo(entityType, entity));
        }

        private final EntityType<?> type;
        private final Class<? extends Entity> clazz;
        // private final Entity instance;
        private final Vec3 box;
        private final List<Tag> tags;
        private int sortId;

        private EntityClassInfo(EntityType<?> entityType, Entity entity) {
            type = entityType;
            clazz = entity.getClass();
            // Do not assign it for now to prevent memory leak because of the client level.
            // instance = null;
            AABB b = entity.getBoundingBox();
            box = new Vec3(b.getXsize(), b.getYsize(), b.getZsize());
            tags = new ArrayList<>();
        }

        public EntityType<?> getType() { return type; }
        public Class<? extends Entity> getClazz() { return clazz; }
        // public Entity getInstance() { return instance; }
        public Vec3 getBox() { return box; }
        public ResourceLocation getLocation() { return EntityType.getKey(getType()); }
        public String getStringId() { return getLocation().toString(); }

        public List<Tag> getTags() { return tags; }
        protected void addTag(Tag tag) { tags.add(tag); }
        protected void removeTag(Tag tag) { tags.remove(tag); }

        public int getSortId() { return sortId; }
        public void setSortId(int sortId) { this.sortId = sortId; }

        @Override
        public String toString() { return type.toString(); }

        @Override
        public int compareTo(EntityManager.EntityClassInfo o) { return sortId - o.sortId; }
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

    public static class Tag implements Comparable<Tag> {
        private final String name;
        private final Component text;
        private final Component description;
        private final List<EntityClassInfo> entities;
        private final Tag father;
        private final List<Tag> sons;

        public Tag(String name) {
            this(name, null);
        }

        public Tag(String tagName, Tag fatherTag) {
            this(tagName, fatherTag, null);
        }

        public Tag(String tagName, Tag fatherTag, Component description) {
            this.name = tagName;
            this.text = Component.translatable(tagName);
            this.description = description;
            this.entities = new ArrayList<>();
            this.father = fatherTag;
            if (this.father != null) {
                this.father.addSon(this);
            }
            this.sons = new ArrayList<>();
        }

        public String getName() { return name; }
        public Component getText() { return text; }
        public Component getDescription() { return description; }

        public List<EntityClassInfo> getEntities() { return entities; }
        protected void addEntity(EntityClassInfo info) { entities.add(info); }
        protected void addEntities(Collection<EntityClassInfo> infoList) { entities.addAll(infoList); }
        protected void removeEntity(EntityClassInfo info) { entities.remove(info); }

        public Tag getFather() { return father; }
        public void addSon(Tag tag) { sons.add(tag); }
        public List<Tag> getSons() { return sons; }

        @Override
        public String toString() { return name; }

        @Override
        public int compareTo(Tag o) {
            String s1 = getName().substring(getName().lastIndexOf('.') + 1);
            String s2 = o.getName().substring(o.getName().lastIndexOf('.') + 1);
            return s1.compareTo(s2);
        }
    }

    public static class TagGroup {
        private final String id;
        private final Component name;
        private final Component description;
        private final Map<String, Tag> tags;
        private final List<Tag> rootTags;

        public TagGroup(String tagGroupName, Component description) {
            this.id = tagGroupName;
            this.name = Component.translatable(tagGroupName);
            this.description = description;
            this.tags = new HashMap<>();
            this.rootTags = new ArrayList<>();
        }

        public String getId() { return id; }
        public Component getName() { return name; }
        public Component getDescription() { return description; }
        public Collection<Tag> getTags() { return tags.values(); }
        public List<Tag> getRootTags() { return rootTags; }

        public boolean containsTag(String tagName) {
            return tags.containsKey(tagName);
        }

        public Tag getTag(String tagName) {
            Tag tag = tags.getOrDefault(tagName, null);
            if (tag == null) {
                throw new RuntimeException("Tag \"" + tagName + "\" doesn't exist.");
            }
            return tag;
        }

        public Tag getOrAddTag(String tagName, Function<String, Tag> func) {
            return tags.computeIfAbsent(tagName, s -> {
                Tag tag = func.apply(s);
                if (tag.getFather() == null) {
                    rootTags.add(tag);
                }
                return tag;
            });
        }

        public void addTag(Tag tag) {
            Tag old = tags.put(tag.getName(), tag);
            if (tag.getFather() == null) {
                rootTags.add(tag);
            }
            if (old != null) {
                throw new RuntimeException("Tag \"" + tag.getName() + "\" already exists.");
            }
        }

        public void addToTag(String tagName, EntityClassInfo entityClassInfo) {
            Tag tag = getTag(tagName);
            tag.addEntity(entityClassInfo);
            entityClassInfo.addTag(tag);
        }

        public void addAllToTag(String tagName, Collection<EntityClassInfo> entityInfos) {
            Tag tag = getTag(tagName);
            tag.addEntities(entityInfos);
            entityInfos.forEach(entityInfo -> entityInfo.addTag(tag));
        }

        public void removeFromTag(String tagName, EntityClassInfo entityClassInfo) {
            Tag tag = getTag(tagName);
            tag.removeEntity(entityClassInfo);
            entityClassInfo.removeTag(tag);
        }

        public void dfsTags(TreeNodeExecutor<Tag> executor) {
            for (Tag root : getRootTags()) {
                dfsTagsPrivate(root, 0, executor);
            }
        }

        private void dfsTagsPrivate(Tag root, int depth, TreeNodeExecutor<Tag> executor) {
            if (executor.execute(root, depth)) {
                int d2 = depth + 1;
                root.getSons().forEach(son -> dfsTagsPrivate(son, d2, executor));
            }
        }
    }

    @FunctionalInterface
    public interface TreeNodeExecutor<E> {
        static <E> TreeNodeExecutor<E> empty() {
            return (cur, depth) -> true;
        }

        boolean execute(E cur, int depth);
    }
}

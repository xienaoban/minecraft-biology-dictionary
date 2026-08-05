package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.config.ConfigsManager;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.Level;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

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

    private final TagGroup defaultTags   = new TagGroup(Lang.TAG_GROUP_DEFAULT,   TextUtils.translate(Lang.TAG_GROUP_DEFAULT_DESC));
    private final TagGroup mcTagTags     = new TagGroup(Lang.TAG_GROUP_TAG,       TextUtils.translate(Lang.TAG_GROUP_TAG_DESC));
    private final TagGroup namespaceTags = new TagGroup(Lang.TAG_GROUP_MODS,      TextUtils.translate(Lang.TAG_GROUP_MODS_DESC));
    private final TagGroup classTags     = new TagGroup(Lang.TAG_GROUP_CLASS,     TextUtils.translate(Lang.TAG_GROUP_CLASS_DESC));
    private final TagGroup interfaceTags = new TagGroup(Lang.TAG_GROUP_INTERFACE, TextUtils.translate(Lang.TAG_GROUP_INTERFACE_DESC));

    private final List<TagGroup> tagGroups = new ArrayList<>(
            Arrays.asList(defaultTags, mcTagTags, namespaceTags, classTags, interfaceTags));

    public EntityManager(Level level) {
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
                entry = new EntityDictionaryEntry(entityType, null);
                entry.markInstanceCreationFailed(e);
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
                if (ia.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
                    return 1;
                }
                if (ib.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
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

    /**
     * @see net.minecraft.tags.EntityTypeTags
     */
    private void initMcTagTagGroups() {
        TagGroup tags = mcTagTags;
        BuiltInRegistries.ENTITY_TYPE.getTags()
                .sorted(DevUtils.getResourceLocationComparator(holders -> holders.key().location()))
                .forEach(holders -> {
                    String key = holders.key().location().toLanguageKey();
                    List<EntityDictionaryEntry> list = holders.stream()
                            .map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get)
                            .map(EntityUtils::getEntityType).map(this::getRawEntityEntry).filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(EntityDictionaryEntry::getSortId))
                            .toList();
                    tags.addTag(new Tag(key, null, TextUtils.literal(holders.key().location().toString())));
                    tags.addAllToTag(key, list);
        });
    }

    private void initJavaTagGroups() {
        String rootClazzName = getClassRealName(Entity.class);
        classTags.addTag(new Tag(rootClazzName, null, TextUtils.literal(rootClazzName)));
        dfsEntityTree(false, (root, depth) -> {
            if (!Modifier.isAbstract(root.getClazz().getModifiers())) {
                return false;
            }
            Tag father = classTags.getTag(getClassRealName(root.getFather().getClazz()));
            String clazzName = getClassRealName(root.getClazz());
            classTags.addTag(new Tag(clazzName, father, TextUtils.literal(clazzName)));
            return true;
        });
        for (EntityDictionaryEntry entry : sortedEntries) {
            String namespace = EntityUtils.getEntityTypeId(entry.getType()).getNamespace();
            namespaceTags.getOrAddTag(namespace,
                    s -> new Tag(s, null, TextUtils.literal(s)));
            namespaceTags.addToTag(namespace, entry);

            Optional<Class<? extends Entity>> clazz = entry.getClazz();
            if (clazz.isEmpty()) { continue; }
            for (Class<? extends Entity> parent : EntityUtils.bottomUp(clazz.get())) {
                String realName = getClassRealName(parent);
                if (classTags.containsTag(realName)) {
                    classTags.addToTag(realName, entry);
                }
                for (Class<?> clazz2 : parent.getInterfaces()) {
                    if (!clazz2.getSimpleName().contains("Mixin")) {
                        String interfazeName = getClassRealName(clazz2);
                        interfaceTags.getOrAddTag(interfazeName,
                                s -> new Tag(s, null, TextUtils.literal(s)));
                        interfaceTags.addToTag(interfazeName, entry);
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
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_INSTANCE_CREATION_FAILED));

        List<EntityDictionaryEntry> friendlyList = new ArrayList<>();
        List<EntityDictionaryEntry> terrestrialList = new ArrayList<>();
        List<EntityDictionaryEntry> humanList = new ArrayList<>();
        List<EntityDictionaryEntry> aquaticList = new ArrayList<>();
        List<EntityDictionaryEntry> bucketableList = new ArrayList<>();
        List<EntityDictionaryEntry> flyingList = new ArrayList<>();
        List<EntityDictionaryEntry> neutralList = new ArrayList<>();
        List<EntityDictionaryEntry> enemyList = new ArrayList<>();
        List<EntityDictionaryEntry> humanoidList = new ArrayList<>();
        List<EntityDictionaryEntry> patrolList = new ArrayList<>();
        List<EntityDictionaryEntry> failedList = new ArrayList<>();

        for (EntityDictionaryEntry entry : sortedEntries) {
            if (entry.isInstanceCreationFailed()) {
                failedList.add(entry);
            }
            Optional<Class<? extends Entity>> clazz = entry.getClazz();
            if (clazz.isEmpty()) { continue; }
            Class<? extends Entity> entityClazz = clazz.get();
            boolean ratio = entry.getType().getHeight() / entry.getType().getWidth() >= 2;
            if (Enemy.class.isAssignableFrom(entityClazz)) {
                enemyList.add(entry);
                if (ratio) {
                    humanoidList.add(entry);
                }

                if (PatrollingMonster.class.isAssignableFrom(entityClazz)) {
                    patrolList.add(entry);
                }
            } else {
                friendlyList.add(entry);
                if (ratio) {
                    humanList.add(entry);
                }

                if (NeutralMob.class.isAssignableFrom(entityClazz)) {
                    neutralList.add(entry);
                }

                if (WaterAnimal.class.isAssignableFrom(entityClazz)
                        || AgeableWaterCreature.class.isAssignableFrom(entityClazz)) {
                    aquaticList.add(entry);
                } else if (entityClazz == Bat.class || entityClazz == Allay.class
                        || entityClazz == Bee.class || entityClazz == HappyGhast.class || entityClazz == Parrot.class) {
                    flyingList.add(entry);
                } else {
                    terrestrialList.add(entry);
                }

                if (Bucketable.class.isAssignableFrom(entityClazz)) {
                    bucketableList.add(entry);
                }
            }
        }
        humanList.add(getRawEntityEntry(EntityTypes.IRON_GOLEM));
        aquaticList.add(getRawEntityEntry(EntityTypes.TURTLE));
        aquaticList.add(getRawEntityEntry(EntityTypes.AXOLOTL));
        aquaticList.add(getRawEntityEntry(EntityTypes.FROG));
        humanList.sort(Comparator.comparingInt(EntityDictionaryEntry::getSortId));
        aquaticList.sort(Comparator.comparingInt(EntityDictionaryEntry::getSortId));

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
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_INSTANCE_CREATION_FAILED, failedList);
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

    public EntityDictionaryEntry getEntityEntry(EntityType<?> entityType) {
        return isEntityTypeBlacklisted(entityType) ? null : getRawEntityEntry(entityType);
    }

    public EntityDictionaryEntry getEntityEntry(Class<? extends Entity> entityClazz) {
        return getEntityEntry(getEntityType(entityClazz));
    }

    public List<EntityDictionaryEntry> getEntityEntries() {
        return sortedEntries.stream().filter(entry -> !isEntityTypeBlacklisted(entry.getType())).toList();
    }

    private EntityDictionaryEntry getRawEntityEntry(EntityType<?> entityType) {
        return entries.get(entityType);
    }

    public boolean isVanillaEntity(EntityType<?> entityType) {
        return Identifier.DEFAULT_NAMESPACE
                .equals(EntityType.getKey(entityType).getNamespace());
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
        public Identifier getId() { return EntityType.getKey(getType()); }
        public String getStringId() { return getId().toString(); }

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

    public static class Tag implements Comparable<Tag> {
        private final String name;
        private final Component text;
        private final Component description;
        private final List<EntityDictionaryEntry> entities;
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
            this.text = TextUtils.translate(tagName);
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

        public List<EntityDictionaryEntry> getEntities() {
            return entities.stream()
                    .filter(entry -> !isEntityTypeBlacklisted(entry.getType())).toList();
        }
        protected void addEntity(EntityDictionaryEntry entry) { entities.add(entry); }
        protected void addEntities(Collection<EntityDictionaryEntry> entryList) { entities.addAll(entryList); }
        protected void removeEntity(EntityDictionaryEntry entry) { entities.remove(entry); }

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
            this.name = TextUtils.translate(tagGroupName);
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

        public void addToTag(String tagName, EntityDictionaryEntry entry) {
            Tag tag = getTag(tagName);
            tag.addEntity(entry);
        }

        public void addAllToTag(String tagName, Collection<EntityDictionaryEntry> entries) {
            Tag tag = getTag(tagName);
            tag.addEntities(entries);
        }

        public void removeFromTag(String tagName, EntityDictionaryEntry entry) {
            Tag tag = getTag(tagName);
            tag.removeEntity(entry);
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

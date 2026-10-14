package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.monster.PatrollingMonster;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds the categories ({@link TagGroup}) of the entity dictionary: the preset groups,
 * the Minecraft tag groups, the mod namespace groups and the class/interface groups.
 * They are built from the {@link EntityManager} entries, once per world session.
 * <p>
 * The biome and structure groups are not here yet: they need the spawn index of the
 * server, and they are added together with that data channel.
 */
public final class EntityCategoryManager {
    private final EntityManager entityManager;

    private final TagGroup defaultTags   = new TagGroup(Lang.TAG_GROUP_DEFAULT,   TextUtils.translate(Lang.TAG_GROUP_DEFAULT_DESC));
    private final TagGroup mcTagTags     = new TagGroup(Lang.TAG_GROUP_TAG,       TextUtils.translate(Lang.TAG_GROUP_TAG_DESC));
    private final TagGroup namespaceTags = new TagGroup(Lang.TAG_GROUP_MODS,      TextUtils.translate(Lang.TAG_GROUP_MODS_DESC));
    private final TagGroup classTags     = new TagGroup(Lang.TAG_GROUP_CLASS,     TextUtils.translate(Lang.TAG_GROUP_CLASS_DESC));
    private final TagGroup interfaceTags = new TagGroup(Lang.TAG_GROUP_INTERFACE, TextUtils.translate(Lang.TAG_GROUP_INTERFACE_DESC));

    private final Map<String, TagGroup> tagGroups = Stream.of(
            defaultTags, mcTagTags, namespaceTags, classTags, interfaceTags)
            .collect(Collectors.toMap(TagGroup::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

    public EntityCategoryManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        initMcTagTagGroups();
        initJavaTagGroups();
        initDefaultTags();
    }

    public Map<String, TagGroup> getTagGroups() {
        return tagGroups;
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
                            .map(EntityUtils::getEntityType).map(entityManager::getRawEntityEntry).filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(EntityDictionaryEntry::getSortId))
                            .toList();
                    tags.addTag(new Tag(key, null, TextUtils.literal(IdentifierUtils.toString(holders.key().location()))));
                    tags.addAllToTag(key, list);
        });
    }

    private void initJavaTagGroups() {
        String rootClazzName = EntityManager.getClassRealName(Entity.class);
        classTags.addTag(new Tag(rootClazzName, null, TextUtils.literal(rootClazzName)));
        entityManager.dfsEntityTree(false, (root, depth) -> {
            if (!Modifier.isAbstract(root.getClazz().getModifiers())) {
                return false;
            }
            Tag father = classTags.getTag(EntityManager.getClassRealName(root.getFather().getClazz()));
            String clazzName = EntityManager.getClassRealName(root.getClazz());
            classTags.addTag(new Tag(clazzName, father, TextUtils.literal(clazzName)));
            return true;
        });
        for (EntityDictionaryEntry entry : entityManager.getRawEntityEntries()) {
            String namespace = EntityUtils.getEntityTypeId(entry.getType()).getNamespace();
            namespaceTags.getOrAddTag(namespace,
                    s -> new Tag(s, null, TextUtils.literal(s)));
            namespaceTags.addToTag(namespace, entry);

            Optional<Class<? extends Entity>> clazz = entry.getClazz();
            if (clazz.isEmpty()) { continue; }
            for (Class<? extends Entity> parent : EntityUtils.bottomUp(clazz.get())) {
                String realName = EntityManager.getClassRealName(parent);
                if (classTags.containsTag(realName)) {
                    classTags.addToTag(realName, entry);
                }
                for (Class<?> clazz2 : parent.getInterfaces()) {
                    if (!clazz2.getSimpleName().contains("Mixin")) {
                        String interfazeName = EntityManager.getClassRealName(clazz2);
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
        defaultTags.addTag(new Tag(Lang.TAG_DEFAULT_BOSS,                 defaultTags.getTag(Lang.TAG_DEFAULT_ENEMY)));
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
        List<EntityDictionaryEntry> bossList = new ArrayList<>();
        List<EntityDictionaryEntry> failedList = new ArrayList<>();

        // `c:bosses` is a cross-platform convention tag (Fabric & NeoForge), already loaded into mcTagTags.
        Set<EntityDictionaryEntry> bossEntries = mcTagTags.containsTag("c.bosses")
                ? new HashSet<>(mcTagTags.getTag("c.bosses").getEntities())
                : Set.of();

        for (EntityDictionaryEntry entry : entityManager.getRawEntityEntries()) {
            if (entry.isInstanceCreationFailed()) {
                failedList.add(entry);
            }
            Optional<Class<? extends Entity>> clazz = entry.getClazz();
            if (clazz.isEmpty()) { continue; }
            Class<? extends Entity> entityClazz = clazz.get();
            boolean ratio = entry.getType().getHeight() / entry.getType().getWidth() >= 2;
            if (EntityUtils.isEnemy(entityClazz)) {
                enemyList.add(entry);
                if (bossEntries.contains(entry)) {
                    bossList.add(entry);
                }
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

                if (EntityUtils.isNeutral(entityClazz)) {
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

        humanList.add(entityManager.getRawEntityEntry(EntityTypes.IRON_GOLEM));
        aquaticList.add(entityManager.getRawEntityEntry(EntityTypes.TURTLE));
        aquaticList.add(entityManager.getRawEntityEntry(EntityTypes.AXOLOTL));
        aquaticList.add(entityManager.getRawEntityEntry(EntityTypes.FROG));
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
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_BOSS, bossList);
        defaultTags.addAllToTag(Lang.TAG_DEFAULT_INSTANCE_CREATION_FAILED, failedList);
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
                    .filter(entry -> !EntityManager.isEntityTypeBlacklisted(entry.getType())).toList();
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
}

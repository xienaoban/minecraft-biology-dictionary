package io.github.xienaoban.biologydictionary.core;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.util.DevUtils;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import io.github.xienaoban.biologydictionary.platform.util.IdentifierUtils;
import io.github.xienaoban.biologydictionary.platform.util.TextUtils;
import io.github.xienaoban.biologydictionary.platform.util.TreeNodeVisitor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.PatrollingMonster;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds the classifications of the entity dictionary: the preset categories, the Minecraft
 * tag categories, the mod namespace categories and the class/interface categories. Each
 * classification owns a tree of {@link Category} objects. They are built from the
 * {@link EntityManager} entries, once per world session.
 * <p>
 * The biome and structure classifications are not here yet: they need the spawn index of
 * the server, and they are added together with that data channel.
 */
public final class EntityClassificationManager {
    private final EntityManager entityManager;

    private final Classification defaultClassification   = new Classification(Lang.CLASSIFICATION_DEFAULT,   TextUtils.translate(Lang.CLASSIFICATION_DEFAULT_DESC));
    private final Classification mcTagClassification     = new Classification(Lang.CLASSIFICATION_TAG,       TextUtils.translate(Lang.CLASSIFICATION_TAG_DESC));
    private final Classification modClassification       = new Classification(Lang.CLASSIFICATION_MODS,      TextUtils.translate(Lang.CLASSIFICATION_MODS_DESC));
    private final Classification classClassification     = new Classification(Lang.CLASSIFICATION_CLASS,     TextUtils.translate(Lang.CLASSIFICATION_CLASS_DESC));
    private final Classification interfaceClassification = new Classification(Lang.CLASSIFICATION_INTERFACE, TextUtils.translate(Lang.CLASSIFICATION_INTERFACE_DESC));

    private final Map<String, Classification> classifications = Stream.of(
            defaultClassification, mcTagClassification, modClassification, classClassification, interfaceClassification)
            .collect(Collectors.toMap(Classification::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

    public EntityClassificationManager(EntityManager entityManager) {
        this.entityManager = entityManager;
        initMcTagClassifications();
        initJavaClassifications();
        initDefaultCategories();
    }

    public Map<String, Classification> getClassifications() {
        return classifications;
    }

    /**
     * @see net.minecraft.tags.EntityTypeTags
     */
    private void initMcTagClassifications() {
        Classification mcTagCategories = mcTagClassification;
        BuiltInRegistries.ENTITY_TYPE.getTags()
                .sorted(DevUtils.getResourceLocationComparator(holders -> holders.key().location()))
                .forEach(holders -> {
                    String key = holders.key().location().toLanguageKey();
                    List<EntityDictionaryEntry> list = holders.stream()
                            .map(Holder::unwrapKey).filter(Optional::isPresent).map(Optional::get)
                            .map(EntityUtils::getEntityType).map(entityManager::getRawEntityEntry).filter(Objects::nonNull)
                            .sorted(Comparator.comparingInt(EntityDictionaryEntry::getSortId))
                            .toList();
                    mcTagCategories.addCategory(new Category(key, null, TextUtils.literal(IdentifierUtils.toString(holders.key().location()))));
                    mcTagCategories.addAllToCategory(key, list);
        });
    }

    private void initJavaClassifications() {
        String rootClazzName = EntityManager.getClassRealName(Entity.class);
        classClassification.addCategory(new Category(rootClazzName, null, TextUtils.literal(rootClazzName)));
        entityManager.dfsEntityTree(false, (root, depth) -> {
            if (!Modifier.isAbstract(root.getClazz().getModifiers())) {
                return false;
            }
            Category father = classClassification.getCategory(EntityManager.getClassRealName(root.getFather().getClazz()));
            String clazzName = EntityManager.getClassRealName(root.getClazz());
            classClassification.addCategory(new Category(clazzName, father, TextUtils.literal(clazzName)));
            return true;
        });
        for (EntityDictionaryEntry entry : entityManager.getRawEntityEntries()) {
            String namespace = EntityUtils.getEntityTypeId(entry.getType()).getNamespace();
            modClassification.getOrAddCategory(namespace,
                    s -> new Category(s, null, TextUtils.literal(s)));
            modClassification.addToCategory(namespace, entry);

            Optional<Class<? extends Entity>> clazz = entry.getClazz();
            if (clazz.isEmpty()) { continue; }
            for (Class<? extends Entity> parent : EntityUtils.bottomUp(clazz.get())) {
                String realName = EntityManager.getClassRealName(parent);
                if (classClassification.containsCategory(realName)) {
                    classClassification.addToCategory(realName, entry);
                }
                for (Class<?> clazz2 : parent.getInterfaces()) {
                    if (!clazz2.getSimpleName().contains("Mixin")) {
                        String interfazeName = EntityManager.getClassRealName(clazz2);
                        interfaceClassification.getOrAddCategory(interfazeName,
                                s -> new Category(s, null, TextUtils.literal(s)));
                        interfaceClassification.addToCategory(interfazeName, entry);
                    }
                }
            }
        }
        interfaceClassification.getRootCategories().sort(DevUtils.getClassNameComparator(Category::getName));
    }

    private void initDefaultCategories() {
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_TERRESTRIAL, defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_HUMANOID,    defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_TERRESTRIAL)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_AQUATIC,     defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_BUCKETABLE,  defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_AQUATIC)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_FLYING,      defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_FRIENDLY_NEUTRAL,     defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_FRIENDLY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_ENEMY));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_ENEMY_HUMANOID,       defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_ENEMY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_ENEMY_PATROL,         defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_ENEMY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_BOSS,                 defaultClassification.getCategory(Lang.CATEGORY_DEFAULT_ENEMY)));
        defaultClassification.addCategory(new Category(Lang.CATEGORY_DEFAULT_INSTANCE_CREATION_FAILED));

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

        // `c:bosses` is a cross-platform convention tag (Fabric & NeoForge), already loaded into mcTagClassification.
        Set<EntityDictionaryEntry> bossEntries = mcTagClassification.containsCategory("c.bosses")
                ? new HashSet<>(mcTagClassification.getCategory("c.bosses").getEntities())
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

                if (EntityUtils.isAquatic(entityClazz)) {
                    aquaticList.add(entry);
                } else if (EntityUtils.isFlying(entityClazz)) {
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

        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY, friendlyList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_TERRESTRIAL, terrestrialList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_HUMANOID, humanList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_AQUATIC, aquaticList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_BUCKETABLE, bucketableList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_FLYING, flyingList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_FRIENDLY_NEUTRAL, neutralList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_ENEMY, enemyList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_ENEMY_HUMANOID, humanoidList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_ENEMY_PATROL, patrolList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_BOSS, bossList);
        defaultClassification.addAllToCategory(Lang.CATEGORY_DEFAULT_INSTANCE_CREATION_FAILED, failedList);
    }

    public static class Category implements Comparable<Category> {
        private final String name;
        private final Component text;
        private final Component description;
        private final List<EntityDictionaryEntry> entities;
        private final Category father;
        private final List<Category> sons;

        public Category(String categoryName) {
            this(categoryName, null);
        }

        public Category(String categoryName, Category fatherCategory) {
            this(categoryName, fatherCategory, null);
        }

        public Category(String categoryName, Category fatherCategory, Component description) {
            this.name = categoryName;
            this.text = TextUtils.translate(categoryName);
            this.description = description;
            this.entities = new ArrayList<>();
            this.father = fatherCategory;
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

        public Category getFather() { return father; }
        public void addSon(Category category) { sons.add(category); }
        public List<Category> getSons() { return sons; }

        @Override
        public String toString() { return name; }

        @Override
        public int compareTo(Category o) {
            String s1 = getName().substring(getName().lastIndexOf('.') + 1);
            String s2 = o.getName().substring(o.getName().lastIndexOf('.') + 1);
            return s1.compareTo(s2);
        }
    }

    public static class Classification {
        private final String id;
        private final Component name;
        private final Component description;
        private final Map<String, Category> categories;
        private final List<Category> rootCategories;

        public Classification(String classificationName, Component description) {
            this.id = classificationName;
            this.name = TextUtils.translate(classificationName);
            this.description = description;
            this.categories = new HashMap<>();
            this.rootCategories = new ArrayList<>();
        }

        public String getId() { return id; }
        public Component getName() { return name; }
        public Component getDescription() { return description; }
        public Collection<Category> getCategories() { return categories.values(); }
        public List<Category> getRootCategories() { return rootCategories; }

        public boolean containsCategory(String categoryName) {
            return categories.containsKey(categoryName);
        }

        public Category getCategory(String categoryName) {
            Category category = categories.getOrDefault(categoryName, null);
            if (category == null) {
                throw new RuntimeException("Category \"" + categoryName + "\" doesn't exist.");
            }
            return category;
        }

        public Category getOrAddCategory(String categoryName, Function<String, Category> func) {
            return categories.computeIfAbsent(categoryName, s -> {
                Category category = func.apply(s);
                if (category.getFather() == null) {
                    rootCategories.add(category);
                }
                return category;
            });
        }

        public void addCategory(Category category) {
            Category old = categories.put(category.getName(), category);
            if (category.getFather() == null) {
                rootCategories.add(category);
            }
            if (old != null) {
                throw new RuntimeException("Category \"" + category.getName() + "\" already exists.");
            }
        }

        public void addToCategory(String categoryName, EntityDictionaryEntry entry) {
            Category category = getCategory(categoryName);
            category.addEntity(entry);
        }

        public void addAllToCategory(String categoryName, Collection<EntityDictionaryEntry> entries) {
            Category category = getCategory(categoryName);
            category.addEntities(entries);
        }

        public void removeFromCategory(String categoryName, EntityDictionaryEntry entry) {
            Category category = getCategory(categoryName);
            category.removeEntity(entry);
        }

        public void dfsCategories(TreeNodeVisitor<Category> visitor) {
            for (Category root : getRootCategories()) {
                dfsCategoriesPrivate(root, 0, visitor);
            }
        }

        private void dfsCategoriesPrivate(Category root, int depth, TreeNodeVisitor<Category> visitor) {
            if (visitor.execute(root, depth)) {
                int d2 = depth + 1;
                root.getSons().forEach(son -> dfsCategoriesPrivate(son, d2, visitor));
            }
        }
    }
}

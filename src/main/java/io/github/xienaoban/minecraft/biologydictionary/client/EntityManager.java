package io.github.xienaoban.minecraft.biologydictionary.client;

import io.github.xienaoban.minecraft.biologydictionary.client.batch.VanillaEntityClassNameAndOrder;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

import static io.github.xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

@Environment(EnvType.CLIENT)
public final class EntityManager {
    private static final EntityManager instance = new EntityManager();

    /**
     * Don't invoke it before joining a world because we need a Minecraft.getInstance().level.
     */
    public static EntityManager getInstance() { return instance; }

    private final Map<Class<?>, EntityTreeNode> tree = new HashMap<>();
    private final Map<EntityType<?>, EntityClassInfo> info = new HashMap<>();
    private final List<EntityClassInfo> sortedInfo = new ArrayList<>();
    private final Map<Class<?>, EntityType<?>> clazzToType = new HashMap<>();

    private final List<TagGroup> tagGroups = new ArrayList<>();

    private final TagGroup defaultTags   = new TagGroup(TranslationKeys.TAG_GROUP_DEFAULT);
    private final TagGroup classTags     = new TagGroup(TranslationKeys.TAG_GROUP_CLASS);
    private final TagGroup interfaceTags = new TagGroup(TranslationKeys.TAG_GROUP_INTERFACE);
    private final TagGroup namespaceTags = new TagGroup(TranslationKeys.TAG_GROUP_NAMESPACE);

    private EntityManager() {
        initEntities();
        initEntitiesSortClassInfo();
        initEntitiesSortTreeNode();
    }

    private void initEntities() {
        tree.put(Entity.class, new EntityTreeNode());
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            EntityClassInfo entityClassInfo;
            try { entityClassInfo = new EntityClassInfo(entityType); }
            catch (NotLivingEntityException ignored) { continue; }
            catch (Exception e) {
                LOGGER.error("Cannot init EntityClassInfo of\"" + EntityType.getKey(entityType) + "\": " + e);
                throw e;
            }
            info.put(entityClassInfo.getType(), entityClassInfo);
            sortedInfo.add(entityClassInfo);
            clazzToType.put(entityClassInfo.getClazz(), entityType);
            getOrCreateEntityTreeNode(entityClassInfo.getClazz());
        }
        // [Should I?] info.put(EntityType.PLAYER, new EntityInfo(EntityType.PLAYER, PlayerEntity.class));
        // [Should I?] getEntityTreeNode(PlayerEntity.class);
    }

    private void initEntitiesSortClassInfo() {
        sortedInfo.sort((a, b) -> {
            Integer oa = VanillaEntityClassNameAndOrder.getMyPreferredOrder(a.getType());
            Integer ob = VanillaEntityClassNameAndOrder.getMyPreferredOrder(b.getType());
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
        for (int i = sortedInfo.size() - 1; i >= 0; --i) {
            sortedInfo.get(i).setSortId(i);
        }
    }

    private void initEntitiesSortTreeNode() {
        for (EntityTreeNode node : tree.values()) {
            node.sortSons();
        }
    }

    private EntityTreeNode getOrCreateEntityTreeNode(Class<?> clazz) {
        EntityTreeNode node = tree.get(clazz);
        if (node == null) {
            node = new EntityTreeNode(clazz, getOrCreateEntityTreeNode(clazz.getSuperclass()));
            tree.put(clazz, node);
        }
        return node;
    }

    public List<TagGroup> getTagGroups() {
        return tagGroups;
    }

    public EntityType<?> getEntityType(Class<?> entityClazz) {
        return clazzToType.get(entityClazz);
    }

    public EntityClassInfo getEntityClassInfo(EntityType<?> entityType) {
        return info.get(entityType);
    }

    public EntityClassInfo getEntityClassInfo(Class<?> entityClazz) {
        return getEntityClassInfo(getEntityType(entityClazz));
    }

    public List<EntityClassInfo> getEntityInfoList() {
        return sortedInfo;
    }

    public boolean isVanillaEntity(EntityType<?> entityType) {
        return ResourceLocation.DEFAULT_NAMESPACE
                .equals(getEntityClassInfo(entityType).getLocation().getNamespace());
    }

    public static String getClassRealName(Class<?> clazz) {
        String res = VanillaEntityClassNameAndOrder.getDeobfuscatedName(clazz);
        if (res == null) res = clazz.getName();
        return res;
    }

    public void dfsEntityTree(boolean skipRoot, TreeNodeExecutor<EntityTreeNode> executor) {
        dfsEntityTree(skipRoot, executor, TreeNodeExecutor.empty());
    }

    public void dfsEntityTree(boolean skipRoot, TreeNodeExecutor<EntityTreeNode> frontExecutor, TreeNodeExecutor<EntityTreeNode> rearExecutor) {
        EntityTreeNode root = tree.get(Entity.class);
        if (skipRoot) {
            root.getSons().forEach(son -> dfsEntityTreePrivate(son, 1, frontExecutor, rearExecutor));
        }
        else {
            dfsEntityTreePrivate(root, 0, frontExecutor, rearExecutor);
        }
    }

    private void dfsEntityTreePrivate(EntityTreeNode root, int depth, TreeNodeExecutor<EntityTreeNode> frontExecutor, TreeNodeExecutor<EntityTreeNode> rearExecutor) {
        if (frontExecutor.execute(root, depth)) {
            int d2 = depth + 1;
            root.getSons().forEach(son -> dfsEntityTreePrivate(son, d2, frontExecutor, rearExecutor));
        }
        rearExecutor.execute(root, depth);
    }

    public static class EntityClassInfo implements Comparable<EntityClassInfo> {
        private final EntityType<?> type;
        private final Class<?> clazz;
        private final Entity instance;
        private final List<Tag> tags;
        private int sortId;

        public EntityClassInfo(EntityType<?> entityType) {
            type = entityType;
            Entity entity = type.create(Minecraft.getInstance().level);
            // Do not assign it for now to prevent memory leak because of the client level.
            instance = null;
            if (!(entity instanceof LivingEntity)) {
                String name = EntityType.getKey(getType()).toString();
                if (entity != null) throw new NotLivingEntityException(name);
                if (type == EntityType.PLAYER) throw new NotLivingEntityException(name);
                throw new NullPointerException("Failed to create \"" + name + "\".");
            }
            clazz = entity.getClass();
            tags = new ArrayList<>();
        }

        public EntityType<?> getType() { return type; }
        public Class<?> getClazz() { return clazz; }
        public Entity getInstance() { return instance; }
        public ResourceLocation getLocation() { return EntityType.getKey(getType()); }
        public String getStringId() { return getLocation().toString(); }

        public List<Tag> getTags() { return tags; }
        protected void addTag(Tag tag) { tags.add(tag); }
        protected void removeTag(Tag tag) { tags.remove(tag); }

        public void setSortId(int sortId) { this.sortId = sortId; }

        @Override
        public String toString() { return type.toString(); }

        @Override
        public int compareTo(EntityManager.EntityClassInfo o) { return sortId - o.sortId; }
    }

    public static class EntityTreeNode {
        private final Class<?> clazz;
        private final EntityTreeNode father;
        private final List<EntityTreeNode> sons;

        public EntityTreeNode() {
            clazz = Entity.class;
            father = null;
            sons = new ArrayList<>();
        }

        public EntityTreeNode(Class<?> entityClazz, EntityTreeNode entityFatherNode) {
            clazz = entityClazz;
            father = entityFatherNode;
            sons = new ArrayList<>();
            entityFatherNode.addSon(this);
        }

        public Class<?> getClazz() { return clazz; }
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
        private final List<EntityClassInfo> entities;
        private final Tag father;
        private final List<Tag> sons;

        public Tag(String name) {
            this(name, null);
        }

        public Tag(String tagName, Tag fatherTag) {
            name = tagName;
            text = Component.translatable(tagName);
            entities = new ArrayList<>();
            father = fatherTag;
            if (father != null) {
                father.addSon(this);
            }
            sons = new ArrayList<>();
        }

        public String getName() { return name; }
        public Component getText() { return text; }

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
        private final String name;
        private final Component text;
        private final Map<String, Tag> tags;
        private final List<Tag> rootTags;

        public TagGroup(String tagGroupName) {
            name = tagGroupName;
            text = Component.translatable(tagGroupName);
            tags = new HashMap<>();
            rootTags = new ArrayList<>();
        }

        public String getName() { return name; }
        public Component getText() { return text; }
        public Collection<Tag> getTags() { return tags.values(); }
        public List<Tag> getRootTags() { return rootTags; }

        public Tag getTag(String tagName) {
            Tag tag = tags.getOrDefault(tagName, null);
            if (tag == null) {
                tag = new Tag(tagName);
                tags.put(tagName, tag);
                rootTags.add(tag);
            }
            return tag;
        }

        public void addTag(String tagName, String fatherTagName) {
            Tag old = tags.put(tagName, new Tag(tagName, getTag(fatherTagName)));
            if (old != null) {
                throw new RuntimeException("Tag \"" + tagName + "\" already exists.");
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

    private static class NotLivingEntityException extends RuntimeException {
        public NotLivingEntityException(String entityName) {
            super("\"" + entityName + "\" is not a LivingEntity!");
        }
    }
}

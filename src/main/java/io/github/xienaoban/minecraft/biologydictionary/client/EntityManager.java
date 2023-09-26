package io.github.xienaoban.minecraft.biologydictionary.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class EntityManager {
    private static volatile EntityManager instance = null;

    /**
     * Don't invoke it before joining a world.
     */
    public static EntityManager getInstance() {
        if (instance == null) {
            synchronized (EntityManager.class) {
                if (instance == null) {
                    instance = new EntityManager();
                }
            }
        }
        return instance;
    }

    private final Map<Class<?>, EntityTreeNode> tree = new HashMap<>();
    private final Map<EntityType<?>, EntityInfo> infos = new HashMap<>();
    private final List<EntityInfo> sortedInfos = new ArrayList<>();

    private EntityManager() {
        initEntityInfos();
    }

    private void initEntityInfos() {
        tree.put(Entity.class, new EntityTreeNode());
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            EntityInfo entityInfo;
            try { entityInfo = new EntityInfo(entityType); }
            catch (Exception e) { continue; }
            infos.put(entityInfo.getType(), entityInfo);
            sortedInfos.add(entityInfo);
            getOrCreateEntityTreeNode(entityInfo.getClazz());
        }
        // [Should I?] this.infos.put(EntityType.PLAYER, new EntityInfo(EntityType.PLAYER, PlayerEntity.class));
        // [Should I?] getEntityTreeNode(PlayerEntity.class);
        initEntityInfoSortIds();
    }

    private void initEntityInfoSortIds() {
        sortedInfos.sort((a, b) -> {
            ResourceLocation ia = EntityType.getKey(a.getType());
            ResourceLocation ib = EntityType.getKey(b.getType());
            Integer sortIdA = this.entitySortIds.getOrDefault(ia.toString(), null);
            Integer sortIdB = this.entitySortIds.getOrDefault(ib.toString(), null);
            if (sortIdA != null && sortIdB != null) {
                return sortIdA - sortIdB;
            }
            else if (sortIdA != null || sortIdB != null) {
                return sortIdA == null ? 1 : -1;
            }
            int cmp = ia.getNamespace().compareTo(ib.getNamespace());
            if (cmp != 0) {
                if (ia.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
                    return 1;
                }
                return cmp;
            }
            String pa = ia.getPath(), pb = ib.getPath();
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
        for (int i = this.sortedInfos.size() - 1; i >= 0; --i) {
            this.sortedInfos.get(i).setSortId(i);
        }
    }

    private EntityTreeNode getOrCreateEntityTreeNode(Class<?> clazz) {
        return tree.computeIfAbsent(clazz, c -> new EntityTreeNode(c, getOrCreateEntityTreeNode(c.getSuperclass())));
    }

    public static class EntityInfo implements Comparable<EntityInfo> {
        private final EntityType<?> type;
        private final Entity instance;
        private final Class<?> clazz;
        private final List<Tag> tags;
        private int sortId;

        public EntityInfo(EntityType<?> type) {
            this.type = type;
            Entity instance = type.create(Minecraft.getInstance().level);
            this.instance = instance;
            if (!(instance instanceof LivingEntity)) {
                throw new RuntimeException("not a LivingEntity?");
            }
            this.clazz = instance.getClass();
            this.tags = new ArrayList<>();
        }

        public EntityType<?> getType() {
            return this.type;
        }

        public Entity getInstance() {
            return this.instance;
        }

        public Class<?> getClazz() {
            return this.clazz;
        }

        public List<Tag> getTags() {
            return this.tags;
        }

        protected void addTag(Tag tag) {
            this.tags.add(tag);
        }

        protected void removeTag(Tag tag) {
            this.tags.remove(tag);
        }

        public void setSortId(int sortId) {
            this.sortId = sortId;
        }

        @Override
        public String toString() {
            return this.type.toString();
        }

        @Override
        public int compareTo(@NotNull EntityInfo o) {
            return this.sortId - o.sortId;
        }
    }

    public static class EntityTreeNode {
        private final Class<?> clazz;
        private final EntityTreeNode father;
        private final List<EntityTreeNode> sons;

        public EntityTreeNode() {
            this.clazz = Entity.class;
            this.father = null;
            this.sons = new ArrayList<>();
        }

        public EntityTreeNode(Class<?> clazz, EntityTreeNode father) {
            this.clazz = clazz;
            this.father = father;
            this.sons = new ArrayList<>();
            father.addSon(this);
        }

        public Class<?> getClazz() {
            return this.clazz;
        }

        public EntityTreeNode getFather() {
            return this.father;
        }

        public List<EntityTreeNode> getSons() {
            return this.sons;
        }

        protected void addSon(EntityTreeNode son) {
            this.sons.add(son);
        }

        @Override
        public String toString() {
            return this.clazz.getSimpleName();
        }
    }

    public static class Tag implements Comparable<Tag> {
        private final String name;
        private final Text text;
        private final List<EntityInfo> entities;
        private final Tag father;
        private final List<Tag> sons;

        public Tag(String name) {
            this(name, null);
        }

        public Tag(String name, Tag father) {
            this.name = name;
            this.text = Text.translatable(name);
            this.entities = new ArrayList<>();
            this.father = father;
            if (father != null) {
                father.addSon(this);
            }
            this.sons = new ArrayList<>();
        }

        public String getName() {
            return this.name;
        }

        public Text getText() {
            return text;
        }

        public List<EntityInfo> getEntities() {
            return this.entities;
        }

        protected void addEntity(EntityInfo info) {
            this.entities.add(info);
        }

        protected void addEntities(Collection<EntityInfo> infos) {
            this.entities.addAll(infos);
        }

        protected void removeEntity(EntityInfo info) {
            this.entities.remove(info);
        }

        public Tag getFather() {
            return father;
        }

        public void addSon(Tag tag) {
            this.sons.add(tag);
        }

        public List<Tag> getSons() {
            return this.sons;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(@NotNull Tag o) {
            String s1 = getName().substring(getName().lastIndexOf('.') + 1);
            String s2 = o.getName().substring(o.getName().lastIndexOf('.') + 1);
            return s1.compareTo(s2);
        }
    }

    public static class TagGroup {
        private final String name;
        private final Text text;
        private final Map<String, Tag> tags;
        private final List<Tag> rootTags;

        public TagGroup(String tagGroupName) {
            this.name = tagGroupName;
            this.text = Text.translatable(tagGroupName);
            this.tags = new HashMap<>();
            this.rootTags = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public Text getText() {
            return text;
        }

        public Collection<Tag> getTags() {
            return this.tags.values();
        }

        public List<Tag> getRootTags() {
            return this.rootTags;
        }

        public Tag getTag(String tagName) {
            Tag tag = this.tags.getOrDefault(tagName, null);
            if (tag == null) {
                tag = new Tag(tagName);
                this.tags.put(tagName, tag);
                this.rootTags.add(tag);
            }
            return tag;
        }

        public void addTag(String tagName, String fatherTagName) {
            Tag old = this.tags.put(tagName, new Tag(tagName, getTag(fatherTagName)));
            if (old != null) {
                throw new RuntimeException("Tag \"" + tagName + "\" already exists.");
            }
        }

        public void addToTag(String tagName, EntityInfo entityInfo) {
            Tag tag = getTag(tagName);
            tag.addEntity(entityInfo);
            entityInfo.addTag(tag);
        }

        public void addAllToTag(String tagName, Collection<EntityInfo> entityInfos) {
            Tag tag = getTag(tagName);
            tag.addEntities(entityInfos);
            entityInfos.forEach(entityInfo -> entityInfo.addTag(tag));
        }

        public void removeFromTag(String tagName, EntityInfo entityInfo) {
            Tag tag = getTag(tagName);
            tag.removeEntity(entityInfo);
            entityInfo.removeTag(tag);
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

package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.EntityManager.TagGroup;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

/**
 * Static facade for entity catalog queries (entity list and tag membership).
 * Reads from the {@link WorldSession} entity manager; no client/server distinction.
 */
public final class EntityInfoApi {
    private EntityInfoApi() {}

    /**
     * The entity dictionary entry for the given entity type, or {@link Optional#empty()}
     * if the world session is unavailable or the type is blacklisted/unknown.
     * Counterpart of {@code EntityManager#getEntityEntry} with an Optional result.
     */
    public static Optional<EntityDictionaryEntry> getEntityEntry(EntityType<?> type) {
        WorldSession ws = WorldSession.get();
        return ws == null ? Optional.empty() : Optional.ofNullable(ws.getEntityManager().getEntityEntry(type));
    }

    /**
     * All trackable entity entries.
     * Returns an empty list if the world session is unavailable.
     */
    public static List<EntityDictionaryEntry> getTotalEntities() {
        WorldSession ws = WorldSession.get();
        return ws == null ? List.of() : ws.getEntityManager().getEntityEntries();
    }

    /**
     * Entity entries of a specific tag in a specific tag group, identified by string keys,
     * e.g. {@code getTagEntities(Lang.TAG_GROUP_DEFAULT, Lang.TAG_DEFAULT_BOSS)}.
     * Returns an empty list if the world session, group or tag is unavailable.
     */
    public static List<EntityDictionaryEntry> getTagEntities(String groupId, String tagId) {
        WorldSession ws = WorldSession.get();
        if (ws == null) { return List.of(); }
        TagGroup group = ws.getEntityManager().getTagGroups().get(groupId);
        if (group == null || !group.containsTag(tagId)) { return List.of(); }
        return group.getTag(tagId).getEntities();
    }

    /**
     * Friendly entity entries from the default {@code friendly} tag.
     * Returns an empty list if the world session or default tag group is unavailable.
     */
    public static List<EntityDictionaryEntry> getFriendlyEntities() {
        return getTagEntities(Lang.TAG_GROUP_DEFAULT, Lang.TAG_DEFAULT_FRIENDLY);
    }

    /**
     * Neutral entity entries from the default {@code friendly_neutral} tag.
     * Returns an empty list if the world session or default tag group is unavailable.
     */
    public static List<EntityDictionaryEntry> getNeutralEntities() {
        return getTagEntities(Lang.TAG_GROUP_DEFAULT, Lang.TAG_DEFAULT_FRIENDLY_NEUTRAL);
    }

    /**
     * Enemy entity entries from the default {@code enemy} tag.
     * Returns an empty list if the world session or default tag group is unavailable.
     */
    public static List<EntityDictionaryEntry> getEnemyEntities() {
        return getTagEntities(Lang.TAG_GROUP_DEFAULT, Lang.TAG_DEFAULT_ENEMY);
    }

    /**
     * Boss entity entries from the default {@code boss} tag
     * (backed by the {@code c:bosses} convention tag).
     * Returns an empty list if the world session or default tag group is unavailable.
     */
    public static List<EntityDictionaryEntry> getBossEntities() {
        return getTagEntities(Lang.TAG_GROUP_DEFAULT, Lang.TAG_DEFAULT_BOSS);
    }
}

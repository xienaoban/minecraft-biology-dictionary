package io.github.xienaoban.biologydictionary.api;

import io.github.xienaoban.biologydictionary.Lang;
import io.github.xienaoban.biologydictionary.core.EntityClassificationManager.Classification;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;

/**
 * Static facade for entity catalog queries (entity list and category membership).
 * Reads from the {@link WorldSession} entity manager and category manager; no
 * client/server distinction.
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
     * Entity entries of a specific category in a specific classification, identified by
     * string keys, e.g.
     * {@code getCategoryEntities(Lang.CLASSIFICATION_DEFAULT, Lang.CATEGORY_DEFAULT_BOSS)}.
     * Returns an empty list if the world session, classification or category is unavailable.
     */
    public static List<EntityDictionaryEntry> getCategoryEntities(String classificationId, String categoryId) {
        WorldSession ws = WorldSession.get();
        if (ws == null) { return List.of(); }
        Classification classification = ws.getEntityClassificationManager().getClassifications().get(classificationId);
        if (classification == null || !classification.containsCategory(categoryId)) { return List.of(); }
        return classification.getCategory(categoryId).getEntities();
    }

    /**
     * Friendly entity entries from the default {@code friendly} category.
     * Returns an empty list if the world session or default classification is unavailable.
     */
    public static List<EntityDictionaryEntry> getFriendlyEntities() {
        return getCategoryEntities(Lang.CLASSIFICATION_DEFAULT, Lang.CATEGORY_DEFAULT_FRIENDLY);
    }

    /**
     * Neutral entity entries from the default {@code friendly_neutral} category.
     * Returns an empty list if the world session or default classification is unavailable.
     */
    public static List<EntityDictionaryEntry> getNeutralEntities() {
        return getCategoryEntities(Lang.CLASSIFICATION_DEFAULT, Lang.CATEGORY_DEFAULT_FRIENDLY_NEUTRAL);
    }

    /**
     * Enemy entity entries from the default {@code enemy} category.
     * Returns an empty list if the world session or default classification is unavailable.
     */
    public static List<EntityDictionaryEntry> getEnemyEntities() {
        return getCategoryEntities(Lang.CLASSIFICATION_DEFAULT, Lang.CATEGORY_DEFAULT_ENEMY);
    }

    /**
     * Boss entity entries from the default {@code boss} category
     * (backed by the {@code c:bosses} convention tag).
     * Returns an empty list if the world session or default classification is unavailable.
     */
    public static List<EntityDictionaryEntry> getBossEntities() {
        return getCategoryEntities(Lang.CLASSIFICATION_DEFAULT, Lang.CATEGORY_DEFAULT_BOSS);
    }
}

package io.github.xienaoban.biologydictionary.core.search;

import io.github.xienaoban.biologydictionary.core.EntityClassificationManager.Category;
import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The first and simplest form of the entity search: a whitespace separated keyword query
 * without any syntax. A keyword matches when it occurs in the selected field, and every
 * keyword has to match; the selected categories are ANDed on top of that.
 * <p>
 * The advanced syntax is not implemented yet; it replaces the internals of this class
 * later.
 */
@ClientOnly
public final class SimpleEntitySearch {
    private SimpleEntitySearch() {}

    public enum Field {
        NAME,
        ID
    }

    /**
     * @param categories selected categories, empty means no category filter. Every selected
     *                   category has to match (AND); a category also matches the entities of
     *                   its descendants
     */
    public static List<EntityDictionaryEntry> search(List<EntityDictionaryEntry> entries, String query,
                                                     Field field, Collection<Category> categories) {
        List<String> keywords = parseKeywords(query);
        List<Set<EntityType<?>>> categoryTypes = collectCategoryTypes(categories);
        if (keywords.isEmpty() && categoryTypes.isEmpty()) { return entries; }

        List<EntityDictionaryEntry> hits = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (!matchesAllCategories(entry, categoryTypes)) { continue; }
            if (keywords.isEmpty() || matches(entry, keywords, field)) { hits.add(entry); }
        }
        return hits;
    }

    private static boolean matchesAllCategories(EntityDictionaryEntry entry,
                                                List<Set<EntityType<?>>> categoryTypes) {
        for (Set<EntityType<?>> types : categoryTypes) {
            if (!types.contains(entry.getType())) { return false; }
        }
        return true;
    }

    private static boolean matches(EntityDictionaryEntry entry, List<String> keywords, Field field) {
        String value = switch (field) {
            case NAME -> EntityUtils.getEntityTypeNameText(entry.getType()).getString();
            case ID -> entry.getStringId();
        };
        String lowerValue = value.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (!lowerValue.contains(keyword)) { return false; }
        }
        return true;
    }

    private static List<Set<EntityType<?>>> collectCategoryTypes(Collection<Category> categories) {
        List<Set<EntityType<?>>> types = new ArrayList<>();
        for (Category category : categories) {
            Set<EntityType<?>> categoryTypes = new HashSet<>();
            collectCategoryTypes(category, categoryTypes);
            types.add(categoryTypes);
        }
        return types;
    }

    private static void collectCategoryTypes(Category category, Set<EntityType<?>> types) {
        for (EntityDictionaryEntry entry : category.getEntities()) {
            types.add(entry.getType());
        }
        for (Category son : category.getSons()) {
            collectCategoryTypes(son, types);
        }
    }

    private static List<String> parseKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        for (String word : query.split("\\s+")) {
            if (word.isEmpty()) { continue; }
            keywords.add(word.toLowerCase(Locale.ROOT));
        }
        return keywords;
    }
}

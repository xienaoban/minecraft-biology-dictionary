package io.github.xienaoban.biologydictionary.core.search;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The first and simplest form of the entity search: a whitespace separated keyword
 * query without any syntax. A keyword matches when it occurs in the selected field,
 * and every keyword has to match.
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

    public static List<EntityDictionaryEntry> search(List<EntityDictionaryEntry> entries,
                                                     String query, Field field) {
        List<String> keywords = parseKeywords(query);
        if (keywords.isEmpty()) { return entries; }

        List<EntityDictionaryEntry> hits = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (matches(entry, keywords, field)) { hits.add(entry); }
        }
        return hits;
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

    private static List<String> parseKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        for (String word : query.split("\\s+")) {
            if (word.isEmpty()) { continue; }
            keywords.add(word.toLowerCase(Locale.ROOT));
        }
        return keywords;
    }
}

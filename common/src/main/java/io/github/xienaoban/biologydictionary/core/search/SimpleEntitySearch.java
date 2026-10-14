package io.github.xienaoban.biologydictionary.core.search;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The first and simplest form of the entity search: a whitespace separated
 * keyword query without any syntax. A keyword matches when it occurs in the
 * entity name or in the entity id, and every keyword has to match.
 * <p>
 * The field selection, the matching options and the advanced syntax are not
 * implemented yet; they replace the internals of this class later.
 */
@ClientOnly
public final class SimpleEntitySearch {
    private SimpleEntitySearch() {}

    public static List<EntityDictionaryEntry> search(List<EntityDictionaryEntry> entries, String query) {
        List<String> keywords = parseKeywords(query);
        if (keywords.isEmpty()) { return entries; }

        List<EntityDictionaryEntry> hits = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (matches(entry, keywords)) { hits.add(entry); }
        }
        return hits;
    }

    private static boolean matches(EntityDictionaryEntry entry, List<String> keywords) {
        String name = EntityUtils.getEntityTypeNameText(entry.getType()).getString().toLowerCase(Locale.ROOT);
        String id = entry.getStringId().toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (!name.contains(keyword) && !id.contains(keyword)) { return false; }
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

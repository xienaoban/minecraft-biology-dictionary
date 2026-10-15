package io.github.xienaoban.biologydictionary.core.search;

import io.github.xienaoban.biologydictionary.core.EntityManager.EntityDictionaryEntry;
import io.github.xienaoban.biologydictionary.platform.ClientOnly;
import io.github.xienaoban.biologydictionary.platform.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The first and simplest form of the entity search: a whitespace separated keyword
 * query without any syntax. A keyword matches when it occurs in the entity name or in
 * the entity id, and every keyword has to match.
 * <p>
 * The field selection and the advanced syntax are not implemented yet; they replace
 * the internals of this class later.
 */
@ClientOnly
public final class SimpleEntitySearch {
    private SimpleEntitySearch() {}

    /**
     * @param fullMatch     the whole candidate value has to match instead of a part of it
     * @param caseSensitive match the case of the query
     * @param regex         interpret every keyword as a regular expression
     */
    public record Options(boolean fullMatch, boolean caseSensitive, boolean regex) {}

    /**
     * @throws PatternSyntaxException if {@link Options#regex()} is on and a keyword is not
     *                                a valid regular expression
     */
    public static List<EntityDictionaryEntry> search(List<EntityDictionaryEntry> entries,
                                                     String query, Options options) {
        List<String> keywords = parseKeywords(query);
        if (keywords.isEmpty()) { return entries; }

        List<Predicate<String>> matchers = new ArrayList<>();
        for (String keyword : keywords) {
            matchers.add(createMatcher(keyword, options));
        }

        List<EntityDictionaryEntry> hits = new ArrayList<>();
        for (EntityDictionaryEntry entry : entries) {
            if (matches(entry, matchers)) { hits.add(entry); }
        }
        return hits;
    }

    private static boolean matches(EntityDictionaryEntry entry, List<Predicate<String>> matchers) {
        String name = EntityUtils.getEntityTypeNameText(entry.getType()).getString();
        String id = entry.getStringId();
        for (Predicate<String> matcher : matchers) {
            if (!matcher.test(name) && !matcher.test(id)) { return false; }
        }
        return true;
    }

    private static Predicate<String> createMatcher(String keyword, Options options) {
        if (options.regex()) {
            // The keyword goes to the regex engine as it is, no query layer unescaping.
            Pattern pattern = Pattern.compile(keyword, options.caseSensitive()
                    ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            return options.fullMatch()
                    ? candidate -> pattern.matcher(candidate).matches()
                    : candidate -> pattern.matcher(candidate).find();
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return candidate -> {
            String value = options.caseSensitive() ? candidate : candidate.toLowerCase(Locale.ROOT);
            String expected = options.caseSensitive() ? keyword : lowerKeyword;
            return options.fullMatch() ? value.equals(expected) : value.contains(expected);
        };
    }

    private static List<String> parseKeywords(String query) {
        List<String> keywords = new ArrayList<>();
        for (String word : query.split("\\s+")) {
            if (word.isEmpty()) { continue; }
            keywords.add(word);
        }
        return keywords;
    }
}

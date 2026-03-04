package io.github.xienaoban.biologydictionary.platform.util;

import java.util.Objects;

public record Pair<F, S>(F first, S second) {
    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    public static <F, S> Pair<F, S> ofFirst(F first) {
        return of(first, null);
    }

    public static <F, S> Pair<F, S> ofSecond(S second) {
        return of(null, second);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) { return true; }
        if (!(other instanceof Pair<?, ?>(Object first1, Object second1))) { return false; }
        return Objects.equals(first, first1) && Objects.equals(second, second1);
    }

    @Override
    public int hashCode() {
        return first.hashCode() * 37 + second.hashCode();
    }
}

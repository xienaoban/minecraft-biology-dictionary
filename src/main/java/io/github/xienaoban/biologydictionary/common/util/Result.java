package io.github.xienaoban.biologydictionary.common.util;

import java.util.Objects;

public record Result<T>(T get) {
    private static final Result<?> EMPTY = new Result<>(null);

    public static<T> Result<T> empty() {
        @SuppressWarnings("unchecked")
        Result<T> t = (Result<T>) EMPTY;
        return t;
    }

    public static <T> Result<T> of(T value) {
        return value == null ? empty() : new Result<>(value);
    }

    public static boolean failed(Result<?> result) {
        return result == null;
    }

    public static <T> Result<T> merge(T t1, T t2) {
        if (t1 == null) {
            return Result.of(t2);
        } else if (t2 == null) {
            return Result.of(t1);
        } else if (Objects.equals(t1, t2)){
            return Result.of(t1);
        } else {
            return null;
        }
    }
}

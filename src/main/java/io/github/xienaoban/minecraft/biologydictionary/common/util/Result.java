package io.github.xienaoban.minecraft.biologydictionary.common.util;

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
}

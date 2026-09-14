package io.github.xienaoban.biologydictionary.platform;

/**
 * Loads platform-specific implementations for common facade classes.
 *
 * <p>Call this directly from a common facade class with {@code Platform.load(Service.class)}.
 * The implementation class must use the facade class name plus {@code Impl}, such as
 * {@code DevUtils -> DevUtilsImpl}, and implement the requested service interface.
 */
public final class Platform {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private Platform() {}

    public static <T> T load(Class<T> serviceType) {
        Class<?> caller = WALKER.walk(frames -> frames
                .skip(1)
                .findFirst()
                .orElseThrow()
                .getDeclaringClass());
        String implementationName = caller.getName() + "Impl";

        try {
            Class<?> implementationClass = Class.forName(implementationName);
            return serviceType.cast(implementationClass.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to load platform implementation: " + implementationName, e);
        }
    }
}

package io.github.xienaoban.biologydictionary.core.widget;

/**
 * Finally I didn't choose to build a verification logic such as:
 * {@snippet :
 *      if (registry.verify(properties)) {
 *          registry.create(properties);
 *      }
 * }
 *
 * But instead I choose to throw this exception in the constructors of widgets:
 * {@snippet :
 *      try {
 *          registry.create(properties);
 *      } catch (UnsupportedWidgetException ignored) {}
 * }
 *
 * Because
 * 1. I don't want to create a registry subclass for each widget class (too many classes
 *    and too many code);
 * 2. The registry classes don't share the inheritance so more code is needed for invoking
 *    verification of widget super class.
 * 3. The current way may not be elegant. But it's intuitive and easy to use.
 */
public final class UnsupportedWidgetException extends RuntimeException {
    private static final UnsupportedWidgetException INSTANCE = new UnsupportedWidgetException();

    public static UnsupportedWidgetException get() {
        return INSTANCE;
    }

    public static void fastThrow() {
        throw INSTANCE;
    }

    public static void verify(boolean condition) {
        if (!condition) {
            fastThrow();
        }
    }

    private UnsupportedWidgetException() {
        super("WTF? Always catch this exception! This exception has no stacktrace.");
    }

    /**
     * Do not waste time to fill in the stack trace. It's useless.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

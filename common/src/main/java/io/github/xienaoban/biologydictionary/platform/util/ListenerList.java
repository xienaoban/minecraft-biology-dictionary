package io.github.xienaoban.biologydictionary.platform.util;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ListenerList<T> {
    private volatile T[] listeners;

    @SuppressWarnings("unchecked")
    public ListenerList(Class<?> listenerClass) {
        listeners = (T[]) Array.newInstance(listenerClass, 0);
    }

    synchronized public void addListener(T listener) {
        int oldLength = listeners.length;
        T[] newListeners = Arrays.copyOf(listeners, oldLength + 1);
        newListeners[oldLength] = listener;
        listeners = newListeners;
    }

    public T[] getListeners() {
        return listeners;
    }
}

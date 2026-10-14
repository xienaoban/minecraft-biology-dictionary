package io.github.xienaoban.biologydictionary.core;

/**
 * Callback of the generic tree walks, see
 * {@link EntityManager#dfsEntityTree} and {@link EntityCategoryManager.TagGroup#dfsTags}.
 */
@FunctionalInterface
public interface TreeNodeExecutor<E> {
    static <E> TreeNodeExecutor<E> empty() {
        return (cur, depth) -> true;
    }

    boolean execute(E cur, int depth);
}

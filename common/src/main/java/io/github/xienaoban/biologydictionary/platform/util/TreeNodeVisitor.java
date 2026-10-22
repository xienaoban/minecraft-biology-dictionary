package io.github.xienaoban.biologydictionary.platform.util;

import io.github.xienaoban.biologydictionary.core.EntityClassificationManager;
import io.github.xienaoban.biologydictionary.core.EntityManager;

/**
 * What a generic tree walk does at each node, see {@link EntityManager#dfsEntityTree} and
 * {@link EntityClassificationManager.Classification#dfsCategories}. Returning false leaves
 * the subtree of the current node unvisited.
 */
@FunctionalInterface
public interface TreeNodeVisitor<E> {
    static <E> TreeNodeVisitor<E> empty() {
        return (cur, depth) -> true;
    }

    boolean execute(E cur, int depth);
}

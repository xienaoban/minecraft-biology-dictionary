package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintNodeVisitor<A> extends AbstractVisitorWrapper<A> {
    private static final Logger LOGGER = LogManager.getLogger();

    private int depth = 0;

    @Override
    public boolean runBefore(Node n, A arg) {
        LOGGER.info("|   ".repeat(depth) + n.getClass().getSimpleName() + ":  " + n);
        ++depth;
        return true;
    }

    @Override
    public boolean runBefore(NodeList<?> n, A arg) {
        LOGGER.info("|   ".repeat(depth) + n.getClass().getSimpleName() + ":  " + n);
        ++depth;
        return true;
    }

    @Override
    protected void runAfter(Node n, A arg) {
        --depth;
    }

    @Override
    protected void runAfter(NodeList<?> n, A arg) {
        --depth;
    }
}
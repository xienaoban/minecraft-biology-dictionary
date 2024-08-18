package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintNodeVisitor<A> extends AbstractVisitorWrapper<A> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public boolean runBefore(Node n, A arg) {
        if (!super.runBefore(n, arg)) return false;
        LOGGER.info("|   ".repeat(depth) + n.getClass().getSimpleName() + ":  " + n);
        return true;
    }

    @Override
    public boolean runBefore(NodeList<?> n, A arg) {
        if (!super.runBefore(n, arg)) return false;
        LOGGER.info("|   ".repeat(depth) + n.getClass().getSimpleName() + ":  " + n);
        return true;
    }
}
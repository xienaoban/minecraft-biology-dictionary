package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadAdditionalNbtVisitor extends AbstractVisitorWrapper<Void> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TARGET = "target";
    private static final String TAG = "compoundTag";

    private final ClassOrInterfaceDeclaration targetClazz;
    private final Class<? extends Entity> entityClazz;
    private boolean superSkipped = false;

    private String curKey;
    private BlockStmt body;
    private boolean done;

    public ReadAdditionalNbtVisitor(ClassOrInterfaceDeclaration targetClazz, Class<? extends Entity> entityClazz) {
        this.targetClazz = targetClazz;
        this.entityClazz = entityClazz;
        reset();
    }

    private void reset() {
        curKey = null;
        body = new BlockStmt();
        done = false;
    }

    private void createMethod() {
        if (curKey == null) {
            throw new AssertionError();
        }
        MethodDeclaration m = targetClazz.addMethod("set_" + entityClazz.getSimpleName() + "_" + curKey, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        m.addParameter(entityClazz, TARGET);
        m.addParameter(CompoundTag.class, TAG);
        m.setBody(body);
        reset();
    }

    @Override
    protected boolean runBefore(Node n, Void arg) {
        if (!super.runBefore(n, arg)) return false;
        LOGGER.info("|   ".repeat(depth) + n.getClass().getSimpleName() + ":  " + n);
        return true;
    }

    @Override
    protected boolean runBefore(NodeList<?> n, Void arg) {
        return super.runBefore(n, arg);
    }

    @Override
    protected void runAfter(Node n, Void arg) {
        super.runAfter(n, arg);
    }

    @Override
    protected void runAfter(NodeList<?> n, Void arg) {
        super.runAfter(n, arg);
    }

    @Override
    public void visit(BlockStmt n, Void arg) {
        if (depth == 0) {
            runBefore(n, arg);
            try {
                n.getStatements().forEach(p -> {
                    body.addStatement(p);
                    p.accept(ReadAdditionalNbtVisitor.this, arg);
                    if (done) createMethod();
                });
            } catch (TagSkipException e) {
                LOGGER.warn("Skip unresolvable tag: " + e.getMessage());
                reset();
            }
            runAfter(n, arg);
            return;
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ExpressionStmt n, Void arg) {
        if (!superSkipped) {
            superSkipped = true;
            boolean[] isSuper = { false };
            n.getExpression().ifMethodCallExpr(methodCallExpr -> methodCallExpr.getScope().ifPresent(expression -> {
                if (expression.isSuperExpr()) {
                    isSuper[0] = true;
                }
            }));
            if (isSuper[0]) {
                reset();
                return;
            } else if (entityClazz != LivingEntity.class) {
                throw new AssertionError("Handle it.");
            }
        }
        super.visit(n ,arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        n.getScope().ifPresent(expression -> expression.ifNameExpr(nameExpr -> {
            // compoundTag.getXXX()
            if (TAG.equals(nameExpr.getName().getIdentifier())) {
                NodeList<Expression> arguments = n.getArguments();
                if (arguments.size() == 2) {
                    Expression second = arguments.get(1);
                    IntegerLiteralExpr integerLiteralExpr = second.asIntegerLiteralExpr();
                    FieldAccessExpr tag = new FieldAccessExpr(new NameExpr(Tag.class.getSimpleName()), TagMap.get(integerLiteralExpr.asNumber().intValue()));
                    n.getArguments().set(1, tag);
                }
                String name = n.getName().getIdentifier();
                if (name.startsWith("get") || name.equals("contains") || name.startsWith("has")) {
                    Expression first = arguments.get(0);
                    StringLiteralExpr stringLiteralExpr = first.asStringLiteralExpr();
                    String newKey = stringLiteralExpr.getValue();
                    if (curKey != null && !curKey.equals(newKey)) {
                        throw new TagSkipException("The key has been set: old=\"" + curKey + "\", new=\"" + stringLiteralExpr.getValue() + "\"");
                    }
                    curKey = newKey;
                    done = true;
                } else {
                    throw new AssertionError("Handle it.");
                }
            }
        }));
    }

    @Override
    public void visit(final ThisExpr n, Void arg) {
        super.visit(n, arg);
        Node parent = n.getParentNode().orElseThrow(AssertionError::new);
        if (parent instanceof FieldAccessExpr fieldAccessExpr) {
            fieldAccessExpr.setScope(new NameExpr(new SimpleName(TARGET)));
        } else if (parent instanceof MethodCallExpr methodCallExpr) {
            methodCallExpr.setScope(new NameExpr(new SimpleName(TARGET)));
        } else {
            throw new AssertionError("Please implement processing logic.");
        }
    }
}

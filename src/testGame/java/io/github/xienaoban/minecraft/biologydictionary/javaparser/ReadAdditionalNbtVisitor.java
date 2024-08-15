package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import net.minecraft.world.entity.Entity;

public class ReadAdditionalNbtVisitor extends AbstractVisitorWrapper<Void> {
    private static final String TARGET = "target";
    private static final String TAG = "compoundTag";

    private final ClassOrInterfaceDeclaration targetClazz;
    private final Class<? extends Entity> entityClazz;

    private String curKey;
    private BlockStmt body;
    private boolean superSkipped = false;

    public ReadAdditionalNbtVisitor(ClassOrInterfaceDeclaration targetClazz, Class<? extends Entity> entityClazz) {
        this.targetClazz = targetClazz;
        this.entityClazz = entityClazz;
        reset();
    }

    private void reset() {
        curKey = null;
        body = new BlockStmt();
    }

    private void createMethod() {
        if (curKey == null) {
            throw new AssertionError();
        }
        MethodDeclaration m = targetClazz.addMethod("set_" + entityClazz.getSimpleName() + "_" + curKey, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        m.addParameter(entityClazz, TARGET);
        m.setBody(body);
        reset();
    }

    @Override
    protected boolean runBefore(Node n, Void arg) {
        if (n instanceof Statement s) {
            body.addStatement(s);
        }
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
    public void visit(ExpressionStmt n, Void arg) {
        boolean[] doSuper = { true };
        if (!superSkipped) {
            n.getExpression().ifMethodCallExpr(methodCallExpr -> methodCallExpr.getScope().ifPresent(expression -> {
                if (expression.isSuperExpr()) {
                    doSuper[0] = false;
                    superSkipped = true;
                }
            }));
        }
        if (doSuper[0]) super.visit(n ,arg);
        else reset();
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        boolean[] doSuper = { true };
        n.getScope().ifPresent(expression -> expression.ifNameExpr(nameExpr -> {
            if (TAG.equals(nameExpr.getName().getIdentifier()) && n.getName().getIdentifier().startsWith("get")) {
                n.getArguments().getFirst().ifPresent(expression1 -> expression1.ifStringLiteralExpr(stringLiteralExpr -> {
                    doSuper[0] = false;
                    curKey = stringLiteralExpr.getValue();
                    createMethod();
                }));
            }
        }));
        if (doSuper[0]) {
            super.visit(n, arg);
        }
    }

    @Override
    public void visit(final ThisExpr n, Void arg) {
        n.getParentNode().ifPresent(parent -> {
            if (parent instanceof FieldAccessExpr fieldAccessExpr) {
                fieldAccessExpr.setScope(new NameExpr(new SimpleName(TARGET)));
            } else {
                throw new AssertionError();
            }
        });
    }
}

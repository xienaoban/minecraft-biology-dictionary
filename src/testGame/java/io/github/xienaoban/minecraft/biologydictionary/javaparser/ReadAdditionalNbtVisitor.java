package io.github.xienaoban.minecraft.biologydictionary.javaparser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

public class ReadAdditionalNbtVisitor extends AbstractVisitorWrapper<Void> {
    private static final Logger LOGGER = createLogger();

    private static final String TARGET = "target";
    private static final String TAG = "compoundTag";

    private static Logger createLogger() {
        FileAppender fileAppender = FileAppender.newBuilder()
                .setName("read-nbt")
                .withFileName("logs/read-nbt.log")
                .build();
        fileAppender.start();
        Logger logger = (Logger) LogManager.getLogger();
        logger.addAppender(fileAppender);
        return logger;
    }

    private final ClassOrInterfaceDeclaration targetClazz;
    private final Class<? extends Entity> entityClazz;
    private boolean superSkipped = false;
    private int genMethodCnt = 0;

    private String curKey;
    private BlockStmt body;
    private boolean foundTagGet;
    private boolean foundTargetSet;

    public ReadAdditionalNbtVisitor(ClassOrInterfaceDeclaration targetClazz, Class<? extends Entity> entityClazz) {
        this.targetClazz = targetClazz;
        this.entityClazz = entityClazz;
        reset();
    }

    public void setFoundTagGet() {
        foundTagGet = true;
        LOGGER.info("!!!! Found tag \"" + curKey + "\" set !!!!");
    }

    public void setFoundTargetSet() {
        foundTargetSet = true;
        LOGGER.info("!!!! Found target set !!!!");
    }

    private void reset() {
        LOGGER.info("!!!! Reset \"" + curKey + "\" !!!!");
        curKey = null;
        body = new BlockStmt();
        foundTagGet = false;
        foundTargetSet = false;
    }

    private void createMethod() {
        if (curKey == null) {
            throw new AssertionError();
        }
        MethodDeclaration m = targetClazz.addMethod("set__" + entityClazz.getSimpleName() + "__" + curKey, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        m.addParameter(entityClazz, TARGET);
        m.addParameter(CompoundTag.class, TAG);
        m.setBody(body);
        genMethodCnt++;
        reset();
    }

    private boolean isGetter(String methodName) {
        return methodName.startsWith("get") || methodName.equals("contains") || methodName.startsWith("has");
    }

    private boolean isSetter(String methodName) {
        return methodName.startsWith("set") || methodName.startsWith("update") || methodName.startsWith("read") || methodName.startsWith("put") || methodName.startsWith("make")
                || methodName.contains("Set") || methodName.startsWith("get");
    }

    @Override
    public void end() {
        if (foundTagGet || foundTargetSet) {
            // throw new AssertionError("Unresolved remaining compoundTag.getXXX() / this.setXXX()");
            LOGGER.warn("Unresolved remaining compoundTag.getXXX() / this.setXXX() for " + entityClazz.getName());
        }

        if (genMethodCnt == 0) {
            targetClazz.addOrphanComment(new BlockComment("No setter for " + entityClazz.getName()));
        }
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
                    if (foundTagGet && foundTargetSet) createMethod();
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
        String methodName = n.getName().getIdentifier();
        n.getScope().ifPresent(expression -> {
            // Try to find `compoundTag.getXXX()`.
            expression.ifNameExpr(nameExpr -> {
                String methodScope = nameExpr.getName().getIdentifier();
                if (TAG.equals(methodScope)) {
                    NodeList<Expression> arguments = n.getArguments();
                    if (arguments.size() == 2) {
                        Expression second = arguments.get(1);
                        IntegerLiteralExpr integerLiteralExpr = second.asIntegerLiteralExpr();
                        FieldAccessExpr tag = new FieldAccessExpr(new NameExpr(Tag.class.getSimpleName()), TagMap.get(integerLiteralExpr.asNumber().intValue()));
                        n.getArguments().set(1, tag);
                    }
                    if (isGetter(methodName)) {
                        Expression first = arguments.get(0);
                        StringLiteralExpr stringLiteralExpr = first.asStringLiteralExpr();
                        String newKey = stringLiteralExpr.getValue();
                        if (curKey != null && !curKey.equals(newKey)) {
                            curKey += "_" + newKey;
                            // throw new TagSkipException("The key has been set: old=\"" + curKey + "\", new=\"" + stringLiteralExpr.getValue() + "\"");
                        } else {
                            curKey = newKey;
                        }
                        setFoundTagGet();
                    } else {
                        throw new AssertionError("Handle it.");
                    }
                }
            });

            // Try to find `this.setXXX()`.
            expression.ifThisExpr(thisExpr -> {
                if (isSetter(methodName)) {
                    setFoundTargetSet();
                }
            });

            // Try to find `this.field.setXXX()`.
            expression.ifFieldAccessExpr(fieldAccessExpr -> {
                fieldAccessExpr.getScope().ifThisExpr(thisExpr -> {
                    if (isSetter(methodName)) {
                        setFoundTargetSet();
                    }
                });
            });
        });
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodReferenceExpr n, Void arg) {
        // Try to find `this::setXXX` or `(T) this::setXXX`.
        boolean[] isTarget = { false };
        n.getScope().ifCastExpr(castExpr -> castExpr.getExpression().ifThisExpr(thisExpr -> isTarget[0] = true));
        n.getScope().ifThisExpr(thisExpr -> isTarget[0] = true);
        if (isTarget[0]) {
            if (isSetter(n.getIdentifier())) {
                setFoundTargetSet();
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(AssignExpr n, Void arg) {
        // Try to find `this.field = xxx`.
        n.getTarget().ifFieldAccessExpr(fieldAccessExpr -> fieldAccessExpr.getScope().ifThisExpr(thisExpr -> {
            setFoundTargetSet();
        }));

        // Try to find `this.field[i] = xxx`.
        n.getTarget().ifArrayAccessExpr(arrayAccessExpr -> {
            arrayAccessExpr.getName().ifFieldAccessExpr(fieldAccessExpr -> {
                fieldAccessExpr.getScope().ifThisExpr(thisExpr -> setFoundTargetSet());
            });
        });
        super.visit(n, arg);
    }

    @Override
    public void visit(final ThisExpr n, Void arg) {
        Node parent = n.getParentNode().orElseThrow(AssertionError::new);
        switch (parent) {
            case FieldAccessExpr fieldAccessExpr -> fieldAccessExpr.setScope(new NameExpr(new SimpleName(TARGET)));
            case MethodCallExpr methodCallExpr -> methodCallExpr.setScope(new NameExpr(new SimpleName(TARGET)));
            case CastExpr castExpr -> castExpr.setExpression(new NameExpr(new SimpleName(TARGET)));
            case null, default -> throw new AssertionError("Please implement processing logic.");
        }
        super.visit(n, arg);
    }
}

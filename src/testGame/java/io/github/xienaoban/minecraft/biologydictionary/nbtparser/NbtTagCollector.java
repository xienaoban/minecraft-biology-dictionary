package io.github.xienaoban.minecraft.biologydictionary.nbtparser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import java.util.*;

public class NbtTagCollector extends AbstractVisitorWrapper<Void> {
    private static final Logger LOGGER = createLogger();

    private static final String TAG_ARG_NAME = "compoundTag";

    public static NbtTagCollector collect(Class<? extends Entity> entityClazz) {
        CompilationUnit source = AstParser.generateAst(entityClazz);
        NbtTagCollector collector = new NbtTagCollector(entityClazz);
        collector.visit(source, null);

        LOGGER.info("NBT tags of entity " + entityClazz + ":");
        for (var e : collector.nbtTags.entrySet()) {
            NbtTagInfo pi = e.getValue();
            LOGGER.info(" - \"" + e.getKey() + "\": " + pi);
        }
        for (var e : collector.conflicts.entrySet()) {
            Set<NbtTagInfo> pis = e.getValue();
            LOGGER.info(" - !\"" + e.getKey() + "\": " + pis.stream().map(NbtTagInfo::getTypeString).toList());
        }
        LOGGER.info("");

        return collector;
    }

    private static Logger createLogger() {
        FileAppender fileAppender = FileAppender.newBuilder()
                .setName("nbt-tag-collector")
                .withFileName("logs/nbt-tag-collector.log")
                .build();
        fileAppender.start();
        Logger logger = (Logger) LogManager.getLogger();
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        return logger;
    }

    private final Class<? extends Entity> entityClazz;
    private final Map<String, NbtTagInfo> nbtTags = new HashMap<>();
    private final Map<String, Set<NbtTagInfo>> conflicts = new HashMap<>();

    private MethodDeclaration currentMethod;

    private NbtTagCollector(Class<? extends Entity> entityClazz) {
        this.entityClazz = entityClazz;
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        currentMethod = n;
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        String methodName = n.getName().getIdentifier();
        n.getScope().ifPresent(expression -> expression.ifNameExpr(nameExpr -> {
            String methodScope = nameExpr.getName().getIdentifier();
            if (TAG_ARG_NAME.equals(methodScope)) {
                // Probably be `compoundTag.getXXX()` or `compoundTag.putXXX()`.
                try {
                    LOGGER.trace("CompoundTag found in " + entityClazz + "." + currentMethod.getNameAsString() + ":\t" + n);
                    NodeList<Expression> arguments = n.getArguments();
                    String nbtTagName = arguments.get(0).asStringLiteralExpr().getValue();
                    if (methodName.startsWith("get")) {
                        parseGetter(nbtTagName, methodName, arguments);
                    } else if (methodName.startsWith("put")) {
                        parsePutter(nbtTagName, methodName, arguments);
                    } else if (methodName.equals("contains") || methodName.equals("remove") || methodName.startsWith("has")) {
                        parseContainer(nbtTagName, methodName, arguments);
                    } else {
                        throw new AssertionError("Handle it");
                    }
                } catch (Throwable e) {
                    throw new AssertionError("Failed to parse method: `" + n + "` in `" + currentMethod.getDeclarationAsString() + "`", e);
                }
            }
        }));
        super.visit(n, arg);
    }

    private void parseGetter(String nbtTagName, String methodName, NodeList<Expression> arguments) {
        TagMap type;
        boolean list;
        if (methodName.equals("getList")) {
            if (arguments.size() != 2) throw new AssertionError(arguments.size());
            Expression second = arguments.get(1);
            IntegerLiteralExpr integerLiteralExpr = second.asIntegerLiteralExpr();
            type = TagMap.getByValue(integerLiteralExpr.asNumber().intValue());
            list = true;
        } else {
            if (arguments.size() != 1) throw new AssertionError(arguments.size());
            type = TagMap.getByGetter(methodName);
            list = false;
        }

        mergeNbtTagInfo(nbtTagName, new NbtTagInfo(type, list, true, false));
    }

    private void parsePutter(String nbtTagName, String methodName, NodeList<Expression> arguments) {
        TagMap type;
        if (methodName.equals("putList")) {
            throw new AssertionError("Handle it");
        } else {
            if (arguments.size() != 2) throw new AssertionError(arguments.size());
            type = TagMap.getByPutter(methodName);
        }

        mergeNbtTagInfo(nbtTagName, new NbtTagInfo(type, false, false, true));
    }

    private void parseContainer(String nbtTagName, String methodName, NodeList<Expression> arguments) {
        TagMap type = switch (methodName) {
            case "contains" -> {
                if (arguments.size() == 1) {
                    yield TagMap.ANY;
                } else if (arguments.size() == 2) {
                    IntegerLiteralExpr integerLiteralExpr = arguments.get(1).asIntegerLiteralExpr();
                    yield TagMap.getByValue(integerLiteralExpr.asNumber().intValue());
                } else {
                    throw new AssertionError(arguments.size());
                }
            }
            case "hasUUID" -> {
                if (arguments.size() != 1) throw new AssertionError(arguments.size());
                yield TagMap.UUID;
            }
            case "remove" -> TagMap.ANY;
            default -> throw new AssertionError("Handle it");
        };

        mergeNbtTagInfo(nbtTagName, new NbtTagInfo(type, false, false, false));
    }

    private void mergeNbtTagInfo(String nbtTagName, NbtTagInfo pi) {
        if (conflicts.containsKey(nbtTagName)) {
            conflicts.get(nbtTagName).add(pi);
            return;
        }

        if (!nbtTags.containsKey(nbtTagName)) {
            nbtTags.put(nbtTagName, pi);
        } else {
            NbtTagInfo pj = nbtTags.get(nbtTagName);
            NbtTagInfo pk;

            boolean hasGetter = pi.hasGetter || pj.hasGetter;
            boolean hasPutter = pi.hasPutter || pj.hasPutter;

            if (Objects.equals(pi, pj)) {
                pk = new NbtTagInfo(pi.type, pj.list, hasGetter, hasPutter);
            } else if (pi.isMorePreciseThan(pj)) {
                pk = new NbtTagInfo(pi.type, pi.list, hasGetter, hasPutter);
            } else if (pj.isMorePreciseThan(pi)) {
                pk = new NbtTagInfo(pj.type, pj.list, hasGetter, hasPutter);
            } else {
                LOGGER.warn("Conflict of \"" + nbtTagName + "\": " + pi + " vs " + pj);
                Set<NbtTagInfo> c = conflicts.computeIfAbsent(nbtTagName, k -> new HashSet<>());
                c.add(pi);
                c.add(pj);
                nbtTags.remove(nbtTagName);
                return;
            }
            nbtTags.put(nbtTagName, pk);
        }
    }

    public record NbtTagInfo(TagMap type, boolean list, boolean hasGetter, boolean hasPutter) {
        public NbtTagInfo {
            if (type.isList()) {
                type = type.removeList();
                if (!list) list = true;
                else LOGGER.warn("Nested arrays are not supported for now.");
            }
        }

        public boolean isMorePreciseThan(NbtTagInfo that) {
            if (this.list == that.list) {
                if (this.type == that.type) return false;
                return switch (that.type) {
                    case TagMap.ANY -> true;
                    case TagMap.ANY_NUMERIC -> this.type.isNumeric();
                    case TagMap.BYTE -> this.type == TagMap.BOOLEAN;
                    case TagMap.INT -> this.type == TagMap.BYTE;
                    default -> false;
                };
            }
            return !that.list && that.type == TagMap.ANY;
        }

        public String getTypeString() {
            return (list ? "[" : "") + type;
        }

        @Override
        public String toString() {
            return "NbtTagInfo{" +
                    "type=" + getTypeString() +
                    ", hasGetter=" + hasGetter +
                    ", hasPutter=" + hasPutter +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NbtTagInfo that = (NbtTagInfo) o;
            return list == that.list && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, list);
        }
    }
}

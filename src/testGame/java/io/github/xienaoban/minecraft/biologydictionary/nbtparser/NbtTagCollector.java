package io.github.xienaoban.minecraft.biologydictionary.nbtparser;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO: NbtUtils.readBlockPos in net.minecraft.world.entity.animal.Bee
 */
public class NbtTagCollector extends AbstractVisitorWrapper<Void> {
    private static final Logger LOGGER = createLogger();

    private static final String TAG_ARG_NAME = "compoundTag";
    private static final String NBT_UTILS_CLASS_NAME = NbtUtils.class.getSimpleName();
    private static final String READ_BLOCK_POS_METHOD_NAME = "readBlockPos";
    private static final String WRITE_BLOCK_POS_METHOD_NAME = "writeBlockPos";

    public static NbtTagCollector collect(Class<? extends Entity> entityClazz) {
        CompilationUnit ast = AstParser.generateAst(entityClazz);
        NbtTagCollector collector = new NbtTagCollector(entityClazz);
        collector.visit(ast, null);

        for (var it = collector.nbtTags.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            var k = e.getKey();
            var v = e.getValue();
            if (v.type == TagMap.ANY || !(v.hasGetter && v.hasPutter)) {
                it.remove();
                collector.addConflict(k, v);
            }
        }
        LOGGER.info("NBT tags of entity " + entityClazz + ":");
        for (var e : collector.nbtTags.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagInfo pi = e.getValue();
            LOGGER.info(" - \"" + e.getKey() + "\": " + pi);
        }
        for (var e : collector.conflicts.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Map<NbtTagInfo, NbtTagInfo> pis = e.getValue();
            LOGGER.info(" - !\"" + e.getKey() + "\": " + pis.values().stream().sorted().toList());
        }
        LOGGER.info("");

        return collector;
    }

    private static Logger createLogger() {
        Path filePath = Path.of(PropertyClazzGenerator.OUTPUT_CLAZZ_PATH.toString(), "a-nbt-tag-list.log");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FileAppender fileAppender = FileAppender.newBuilder()
                .setName("nbt-tag-collector")
                .withFileName(filePath.toString())
                .build();
        fileAppender.start();
        Logger logger = (Logger) LogManager.getLogger();
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
        return logger;
    }

    private final Class<? extends Entity> entityClazz;
    private final Map<String, NbtTagInfo> nbtTags = new HashMap<>();
    private final Map<String, Map<NbtTagInfo, NbtTagInfo>> conflicts = new HashMap<>();

    private MethodDeclaration currentMethod;

    private NbtTagCollector(Class<? extends Entity> entityClazz) {
        this.entityClazz = entityClazz;
    }

    public Map<String, NbtTagInfo> getNbtTags() {
        return nbtTags;
    }

    public Map<String, Map<NbtTagInfo, NbtTagInfo>> getConflicts() {
        return conflicts;
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
            try {
                if (TAG_ARG_NAME.equals(methodScope)) {
                    // Probably be `compoundTag.getXXX()` or `compoundTag.putXXX()`.
                    LOGGER.trace("CompoundTag found in " + entityClazz + "." + currentMethod.getNameAsString() + ":\t" + n);
                    NodeList<Expression> arguments = n.getArguments();
                    String nbtTagName = arguments.get(0).asStringLiteralExpr().getValue();
                    if (methodName.startsWith("get")) {
                        parseGetter(nbtTagName, methodName, arguments);
                    } else if (methodName.startsWith("put")) {
                        parsePutter(nbtTagName, methodName, arguments);
                    } else if (methodName.equals("contains") || methodName.startsWith("has")) {
                        parseContainer(nbtTagName, methodName, arguments);
                    } else if (methodName.equals("remove")) {
                        // ignore
                    } else {
                        throw new AssertionError("Handle it");
                    }
                } else if (NBT_UTILS_CLASS_NAME.equals(methodScope)) {
                    // Probably be `NbtUtils.readBlockPos()` or `NbtUtils.writeBlockPos()`.
                    LOGGER.trace("CompoundTag found in " + entityClazz + "." + currentMethod.getNameAsString() + ":\t" + n);
                    NodeList<Expression> arguments = n.getArguments();

                    if (methodName.equals(READ_BLOCK_POS_METHOD_NAME)) {
                        String tagArgName = arguments.get(0).asNameExpr().getNameAsString();
                        String nbtTagName = arguments.get(1).asStringLiteralExpr().getValue();
                        if (TAG_ARG_NAME.equals(tagArgName)) {
                            mergeNbtTagInfo(nbtTagName, new NbtTagInfo(TagMap.BLOCK_POS, false, true, false));
                        }
                    }
                }
            } catch (Throwable e) {
                throw new AssertionError("Failed to parse method: `" + n + "` in `" + currentMethod.getDeclarationAsString() + "`", e);
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
            if (type == TagMap.ANY && arguments.get(1).isMethodCallExpr()) {
                MethodCallExpr call = arguments.get(1).asMethodCallExpr();
                try {
                    String methodScope2 = call.getScope().orElseThrow().asNameExpr().getName().getIdentifier();
                    String methodName2 = call.getName().getIdentifier();
                    if (NBT_UTILS_CLASS_NAME.equals(methodScope2) && WRITE_BLOCK_POS_METHOD_NAME.equals(methodName2)) {
                        type = TagMap.BLOCK_POS;
                    }
                } catch (Exception ignored) {}
            }
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
            default -> throw new AssertionError("Handle it");
        };

        mergeNbtTagInfo(nbtTagName, new NbtTagInfo(type, false, false, false));
    }

    private void mergeNbtTagInfo(String nbtTagName, NbtTagInfo pi) {
        if (conflicts.containsKey(nbtTagName)) {
            addConflict(nbtTagName, pi);
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
                LOGGER.debug("Conflict of \"" + nbtTagName + "\": " + pi + " vs " + pj);
                addConflict(nbtTagName, pi);
                addConflict(nbtTagName, pj);
                nbtTags.remove(nbtTagName);
                return;
            }
            nbtTags.put(nbtTagName, pk);
        }
    }

    private void addConflict(String nbtTagName, NbtTagInfo pi) {
        Map<NbtTagInfo, NbtTagInfo> map = conflicts.computeIfAbsent(nbtTagName, s -> new HashMap<>());
        NbtTagInfo key = new NbtTagInfo(pi.type, pi.list, false, false);
        if (!map.containsKey(pi)) {
            map.put(key, pi);
        } else {
            NbtTagInfo pj = map.get(key);
            boolean hasGetter = pi.hasGetter || pj.hasGetter;
            boolean hasPutter = pi.hasPutter || pj.hasPutter;
            map.put(key, new NbtTagInfo(key.type, key.list, hasGetter, hasPutter));
        }
    }

    public record NbtTagInfo(TagMap type, boolean list, boolean hasGetter, boolean hasPutter) implements Comparable<NbtTagInfo> {
        public NbtTagInfo {
            if (type.isList()) {
                type = type.removeList();
                if (!list) list = true;
                else LOGGER.warn("Nested arrays are not supported for now.");
            }
        }

        public boolean isMorePreciseThan(NbtTagInfo that) {
            if (this.list == that.list) {
                return this.type.isMorePreciseThan(that.type);
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

        @Override
        public int compareTo(NbtTagCollector.NbtTagInfo that) {
            int c = Integer.compare(this.type.getId(), that.type.getId());
            if (c == 0) {
                if (this.list == that.list) {
                    return 0;
                }
                return this.list ? 1 : -1;
            }
            return c;
        }
    }
}

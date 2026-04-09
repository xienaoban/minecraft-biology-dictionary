package io.github.xienaoban.biologydictionary.core.property;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.xienaoban.biologydictionary.core.session.WorldSession;
import io.github.xienaoban.biologydictionary.platform.util.Misc;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: NbtUtils.readBlockPos in net.minecraft.world.entity.animal.Bee
 */
public class NbtTagCollector extends AbstractVisitorWrapper<Void> {
    private static final Path LOGGER_PATH = Path.of(PropertyClazzGenerator.OUTPUT_CLAZZ_DIR_PATH.toString(), ".nbt-tag-list.log");
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_ARG_NAME = "compoundTag";

    private static final String NBT_UTILS_CLASS_NAME = NbtUtils.class.getSimpleName();
    private static final String READ_BLOCK_POS_METHOD_NAME = "readBlockPos";
    private static final String WRITE_BLOCK_POS_METHOD_NAME = "writeBlockPos";

    private static final String ITEM_STACK_CLASS_NAME = ItemStack.class.getSimpleName();
    private static final String READ_ITEM_STACK_METHOD_NAME = "parse";
    private static final String READ_OR_NULL_ITEM_STACK_METHOD_NAME = "parseOptional";

    private static Map<Class<? extends Entity>, NbtTagCollector> allNbts = null;

    private static BufferedWriter nbtFileWriter = null;

    public static NbtTagCollector get(Class<? extends Entity> entityClazz) {
        Objects.requireNonNull(allNbts);
        return Objects.requireNonNull(allNbts.get(entityClazz));
    }

    public static void collectAll() {
        allNbts = new HashMap<>();
        try {
            Files.deleteIfExists(LOGGER_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(LOGGER_PATH)) {
            nbtFileWriter = writer;
            WorldSession.get().getEntityManager().dfsEntityTree(true, (cur, depth) -> {
                Class<? extends Entity> entityClazz = cur.getClazz();
                LOGGER.info("Testing {}", entityClazz);
                NbtTagCollector collector = collect(entityClazz);
                allNbts.put(entityClazz, collector);
                return true;
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            nbtFileWriter = null;
        }
    }

    public static void loadAll() {
        allNbts = new HashMap<>();

        String regexGood = """
                ^ - "([^"]+)": (.+)$
                """.replaceAll("[\r\n]", "");
        Pattern patternGood = Pattern.compile(regexGood);

        String regexBad = """
                ^ - !"([^"]+)": \\[(.+)\\]$
                """.replaceAll("[\r\n]", "");
        Pattern patternBad = Pattern.compile(regexBad);

        int lineNumber = 0;
        try {
            NbtTagCollector collector = null;
            for (String line : Files.readAllLines(LOGGER_PATH)) {
                ++lineNumber;
                if (line.startsWith("NBT tags of entity class ")) {
                    String clazzName = line.substring(line.lastIndexOf(' ') + 1, line.length() - 1);
                    Class<? extends Entity> entityClazz = Misc.cast(Class.forName(clazzName));
                    logAndWrite("NBT tags of entity " + entityClazz + ":");
                    collector = new NbtTagCollector(entityClazz);
                    allNbts.put(entityClazz, collector);
                } else if (line.startsWith(" - !")) {
                    Matcher matcher = patternBad.matcher(line);
                    if (!matcher.find()) {
                        throw new RuntimeException("Matcher not found!");
                    }
                    String nbtName = matcher.group(1);
                    Map<NbtTagInfo, NbtTagInfo> map = new HashMap<>();
                    Matcher matcher2 = NbtTagInfo.DE_PATTERN.matcher(matcher.group(2));
                    NbtTagInfo tag;
                    while ((tag = NbtTagInfo.deserialize(matcher2)) != null) {
                        map.put(tag, tag);
                    }
                    Objects.requireNonNull(collector).conflicts.put(nbtName, map);
                } else if (line.startsWith(" - ")) {
                    Matcher matcher = patternGood.matcher(line);
                    if (!matcher.find()) {
                        throw new RuntimeException("Matcher not found!");
                    }
                    String nbtName = matcher.group(1);
                    Matcher matcher2 = NbtTagInfo.DE_PATTERN.matcher(matcher.group(2));
                    Objects.requireNonNull(collector).nbtTags.put(nbtName, NbtTagInfo.deserialize(matcher2));
                } else if (line.isEmpty()) {
                    collector = null;
                } else {
                    throw new RuntimeException("Unknown line!");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Pars line " + lineNumber + " error!", e);
        }
    }


    private static NbtTagCollector collect(Class<? extends Entity> entityClazz) {
        CompilationUnit ast = AstParser.generateAst(entityClazz);
        NbtTagCollector collector = new NbtTagCollector(entityClazz);
        collector.visit(ast, null);

        for (var it = collector.nbtTags.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            var k = e.getKey();
            var v = e.getValue();
            if (!(v.hasGetter && v.hasPutter) || v.type == TagMap.ANY || v.type == TagMap.COMPOUND) {
                it.remove();
                collector.addConflict(k, v);
            }
        }
        logAndWrite("NBT tags of entity " + entityClazz + ":");
        for (var e : collector.nbtTags.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagInfo pi = e.getValue();
            logAndWrite(" - \"" + e.getKey() + "\": " + pi);
        }
        for (var e : collector.conflicts.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Map<NbtTagInfo, NbtTagInfo> pis = e.getValue();
            logAndWrite(" - !\"" + e.getKey() + "\": " + pis.values().stream().sorted().toList());
        }
        logAndWrite("");

        return collector;
    }

    private static void logAndWrite(String line) {
        LOGGER.info(line);
        if (nbtFileWriter != null) {
            try {
                nbtFileWriter.write(line);
                nbtFileWriter.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Class<? extends Entity> entityClazz;
    private final Map<String, NbtTagInfo> nbtTags = new HashMap<>();
    private final Map<String, Map<NbtTagInfo, NbtTagInfo>> conflicts = new HashMap<>();

    private MethodDeclaration currentMethod;
    private String currentPropertyName;

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
                } else if (ITEM_STACK_CLASS_NAME.equals(methodScope)) {
                    // Probably be `ItemStack.parse()` or `ItemStack.parseOptional()`.
                    LOGGER.trace("CompoundTag found in " + entityClazz + "." + currentMethod.getNameAsString() + ":\t" + n);
                    NodeList<Expression> arguments = n.getArguments();

                    // For `this.bodyArmorItem = (ItemStack)ItemStack.parse(this.registryAccess(), compoundTag.getCompound("body_armor_item")).orElse(ItemStack.EMPTY);`
                    arguments.get(1).ifMethodCallExpr(methodCallExpr1 -> methodCallExpr1.getScope().ifPresent(expression1 -> expression1.ifNameExpr(nameExpr1 -> {
                        String methodScope1 = nameExpr1.getName().getIdentifier();
                        if (TAG_ARG_NAME.equals(methodScope1)) {
                            super.visit(methodCallExpr1, arg);
                        }
                    })));

                    if (methodName.equals(READ_ITEM_STACK_METHOD_NAME) || methodName.equals(READ_OR_NULL_ITEM_STACK_METHOD_NAME)) {
                        if (currentPropertyName != null && nbtTags.containsKey(currentPropertyName)) {
                            boolean isList = nbtTags.get(currentPropertyName).list();
                            mergeNbtTagInfo(currentPropertyName, new NbtTagInfo(TagMap.ITEM_STACK, isList, true, false));
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
        currentPropertyName = nbtTagName;
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

        private static final String DE_REGEX = """
                NbtTagInfo\\{type=([^,]+), hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
        private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

        private static NbtTagInfo deserialize(Matcher matcher) {
            if (!matcher.find()) {
                return null;
            }
            String t = matcher.group(1);
            boolean isList = t.startsWith("[") && t.endsWith("]");
            TagMap type = TagMap.valueOf(isList ? t.substring(1, t.length() - 1) : t);
            boolean hasGetter = Boolean.getBoolean(matcher.group(2));
            boolean hasPutter = Boolean.getBoolean(matcher.group(3));
            return new NbtTagInfo(type, isList, hasGetter, hasPutter);
        }

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
            return (list ? ("[" + type.name() + "]") : type.name());
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

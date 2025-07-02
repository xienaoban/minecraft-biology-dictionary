package io.github.xienaoban.minecraft.biologydictionary.core.property;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import io.github.xienaoban.minecraft.biologydictionary.common.util.Misc;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.util.TestUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.variant.VariantUtils;
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

public class NbtTagCollector extends AbstractVisitorWrapper<Void> {
    private static final Path LOGGER_PATH = Path.of(PropertyClazzGenerator.OUTPUT_CLAZZ_DIR_PATH.toString(), ".nbt-tag-list.log");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean PRINT_TAG_METHODS = false;

    private static final String VALUE_INPUT_NAME = "valueInput";
    private static final String VALUE_OUTPUT_NAME = "valueOutput";

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
            EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
                Class<? extends Entity> entityClazz = cur.getClazz();
                // if (entityClazz != LivingEntity.class) return true;
                LOGGER.info("Testing {}", entityClazz);
                NbtTagCollector collector = collect(entityClazz);
                allNbts.put(entityClazz, collector);
                return true;
            });
            ClassTypeCollector.storeImport();
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
                    collector = new NbtTagCollector(entityClazz, Objects.requireNonNull(null));
                    allNbts.put(entityClazz, collector);
                } else if (line.startsWith(" - !")) {
                    Matcher matcher = patternBad.matcher(line);
                    if (!matcher.find()) {
                        throw new RuntimeException("Matcher not found!");
                    }
                    String nbtName = matcher.group(1);
                    Map<NbtTagInfo, NbtTagInfo> map = new HashMap<>();
                    for (String s : matcher.group(2).split(", ")) {
                        NbtTagInfo tag = NbtTagInfo.deserialize(s);
                        map.put(tag, tag);
                    }
                    Objects.requireNonNull(collector).conflicts.put(nbtName, map);
                } else if (line.startsWith(" - ")) {
                    Matcher matcher = patternGood.matcher(line);
                    if (!matcher.find()) {
                        throw new RuntimeException("Matcher not found!");
                    }
                    String nbtName = matcher.group(1);
                    Objects.requireNonNull(collector).nbtTags.put(nbtName, NbtTagInfo.deserialize(matcher.group(2)));
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
        ClassTypeCollector knownTypes = new ClassTypeCollector(entityClazz);
        knownTypes.visit(ast,null);
        NbtTagCollector collector = new NbtTagCollector(entityClazz, knownTypes);
        collector.visit(ast, null);

        logAndWrite("NBT tags of entity " + entityClazz + ":");
        for (var e : collector.nbtTags.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagInfo pi = e.getValue();
            logAndWrite(" - \"" + e.getKey() + "\": " + pi);
        }
        for (var e : collector.conflicts.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Map<NbtTagInfo, NbtTagInfo> pis = e.getValue();
            logAndWrite(" - !\"" + e.getKey() + "\": " + pis.values());
        }
        logAndWrite("");

        return collector;
    }

    private static String removeOptional(String raw) {
        if (raw == null) { return null; }
        if (raw.startsWith("Optional<")) {
            return raw.substring("Optional<".length(), raw.length() - 1);
        }
        return raw;
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
    private final ClassTypeCollector knownTypes;

    private MethodDeclaration currentMethod;
    private String currentPropertyName;

    private NbtTagCollector(Class<? extends Entity> entityClazz, ClassTypeCollector knownTypes) {
        this.entityClazz = entityClazz;
        this.knownTypes = knownTypes;
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

        // Print the method nodes.
        if (PRINT_TAG_METHODS) {
            String methodName = n.getNameAsString();
            if (TestUtils.ENTITY_READ_ADDITIONAL_NBT.equals(methodName) || TestUtils.ENTITY_WRITE_ADDITIONAL_NBT.equals(methodName)
                    || TestUtils.ENTITY_READ_NBT.equals(methodName) || TestUtils.ENTITY_WRITE_NBT.equals(methodName)) {
                PrintNodeVisitor<Void> printer = new PrintNodeVisitor<>();
                LOGGER.info("Now print method {}", methodName);
                printer.visit(n, null);
            }
        }

        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        String methodName = n.getName().getIdentifier();
        NodeList<Expression> arguments = n.getArguments();

        try {
            if (n.getScope().orElse(null) instanceof Expression scopeExpression) {
                String methodScope = scopeExpression.toString();
                if (VALUE_INPUT_NAME.equals(methodScope)) {
                    String nbtTagName = arguments.get(0).asStringLiteralExpr().getValue();
                    LOGGER.trace(VALUE_INPUT_NAME + ".func() for {} found in {}.{}:\t{}",
                            nbtTagName, entityClazz, currentMethod.getNameAsString(), n);
                    if (methodName.startsWith("get")) {
                        parseGetter(nbtTagName, methodName);
                    } else if (methodName.startsWith("read")) {
                        parseReader(nbtTagName, n);
                    }
                } else if (VALUE_OUTPUT_NAME.equals(methodScope)) {
                    String nbtTagName = arguments.get(0).asStringLiteralExpr().getValue();
                    LOGGER.trace(VALUE_OUTPUT_NAME + ".func() for {} found in {}.{}:\t{}",
                            nbtTagName, entityClazz, currentMethod.getNameAsString(), n);
                    if (methodName.startsWith("put")) {
                        parsePutter(nbtTagName, methodName);
                    } else if (methodName.startsWith("store")) {
                        parseStorer(nbtTagName, n);
                    }
                } else if (arguments.size() == 2 || arguments.size() == 3) {
                    String valueInputOutput = null;
                    String nbtTagName = null;
                    int valueInputOutputIdx = -1;
                    int nbtTagNameIdx = -1;
                    for (int i = 0; i < arguments.size(); ++i) {
                        Expression e = arguments.get(i);
                        if (e instanceof NameExpr nameExpr) {
                            String s = nameExpr.getName().getIdentifier();
                            if (VALUE_INPUT_NAME.equals(s) || VALUE_OUTPUT_NAME.equals(s)) {
                                if (valueInputOutputIdx != -1) {
                                    throw new AssertionError();
                                }
                                valueInputOutput = s;
                                valueInputOutputIdx = i;
                            }
                        } else if (e instanceof StringLiteralExpr stringLiteralExpr) {
                            if (nbtTagNameIdx != -1) {
                                throw new AssertionError();
                            }
                            nbtTagName = stringLiteralExpr.getValue();
                            nbtTagNameIdx = i;
                        }
                    }
                    if (valueInputOutput != null) {
                        if (VALUE_INPUT_NAME.equals(valueInputOutput)) {
                            LOGGER.trace("func(" + VALUE_INPUT_NAME + ", ...) for {} found in {}.{}:\t{}",
                                    nbtTagName, entityClazz, currentMethod.getNameAsString(), n);
                            parseCalleeReader(nbtTagName, methodScope, methodName, n);
                        } else if (VALUE_OUTPUT_NAME.equals(valueInputOutput)) {
                            LOGGER.trace("func(" + VALUE_OUTPUT_NAME + ", ...) for {} found in {}.{}:\t{}",
                                    nbtTagName, entityClazz, currentMethod.getNameAsString(), n);
                            parseCalleeStorer(nbtTagName, methodScope, methodName, n);
                        }
                    }

                }
            }
        } catch (Throwable e) {
            throw new AssertionError("Failed to parse method: `" + n + "` in `" + currentMethod.getDeclarationAsString() + "`", e);
        }
        super.visit(n, arg);
    }

    private void parseGetter(String nbtTagName, String methodName) {
        if (methodName.endsWith("Or")) {
            methodName = methodName.substring(0, methodName.length() - 2);
        }
        TagMap type = TagMap.getByGetter(methodName);
        mergeNbtTagInfo(nbtTagName, new BuiltinTagInfo(type, true, false));
    }

    private void parseReader(String nbtTagName, MethodCallExpr currNode) {
        NodeList<Expression> arguments = currNode.getArguments();
        String codec = arguments.get(1).toString();
        String type = null;
        Node curr = currNode;
        label: while (curr.getParentNode().isPresent()) {
            Node next = curr.getParentNode().get();
            switch (next) {
                case ExpressionStmt ignored:
                    break label;
                case MethodCallExpr methodCallExpr:
                    if (methodCallExpr.getScope().orElse(null) instanceof ThisExpr) {
                        int idx = methodCallExpr.getArguments().indexOf(curr);
                        if (idx < 0) {
                            throw new AssertionError();
                        }
                        type = knownTypes.getMethodArgType(methodCallExpr.getNameAsString(), idx);
                        break label;
                    }
                    break;
                case AssignExpr assignExpr:
                    if (assignExpr.getOperator().equals(AssignExpr.Operator.ASSIGN)
                            && assignExpr.getTarget() instanceof FieldAccessExpr fieldAccessExpr && fieldAccessExpr.getScope() instanceof ThisExpr) {
                        type = knownTypes.getFieldType(fieldAccessExpr.getNameAsString());
                        break label;
                    }
                    break;
                case VariableDeclarator variableDeclarator:
                    type = knownTypes.getFullyQualifiedType(variableDeclarator.getTypeAsString());
                    break label;
                default:
                    break;
            }
            curr = next;
        }
        mergeNbtTagInfo(nbtTagName, new CodecTagInfo(knownTypes.getFullyQualifiedType(codec), removeOptional(type), true, false));
    }

    private void parseCalleeReader(String nbtTagName, String methodScope, String methodName, MethodCallExpr currNode) {
        if (EntityReference.class.getSimpleName().equals(methodScope)) {
            mergeNbtTagInfo(nbtTagName, new FuncTagInfo(methodScope, methodName, null, "EntityReference<?>", true, false));
        } else if (VariantUtils.class.getSimpleName().equals(methodScope)) {
            if (nbtTagName != null) {
                throw new AssertionError("Should be something like `VariantUtils.readVariant(valueInput, Registries.CAT_VARIANT)`");
            }
            mergeNbtTagInfo(VariantUtils.TAG_VARIANT, new FuncTagInfo(methodScope, methodName, null, "Holder<?>", true, false));
        } else if (nbtTagName != null) {
            mergeNbtTagInfo(nbtTagName, new FuncTagInfo(methodScope, methodName, null, null, true, false));
        } else if (!(currNode.getScope().orElse(null) instanceof ThisExpr)) {
            throw new RuntimeException("Unknown tag: `" + currNode + "` in `" + currentMethod.getDeclarationAsString() + "`");
        }
    }

    private void parsePutter(String nbtTagName, String methodName) {
        TagMap type = TagMap.getByPutter(methodName);
        mergeNbtTagInfo(nbtTagName, new BuiltinTagInfo(type, false, true));
    }

    private void parseStorer(String nbtTagName, MethodCallExpr currNode) {
        NodeList<Expression> arguments = currNode.getArguments();
        String codec = arguments.get(1).toString();
        String type = null;
        Expression toStore = currNode.getArguments().get(2);
        if (toStore instanceof FieldAccessExpr fieldAccessExpr && fieldAccessExpr.getScope() instanceof ThisExpr) {
            type = knownTypes.getFieldType(fieldAccessExpr.getNameAsString());
        } else if (toStore instanceof MethodCallExpr methodCallExpr && methodCallExpr.getScope().orElse(null) instanceof ThisExpr) {
            type = knownTypes.getMethodRetType(methodCallExpr.getNameAsString());
        }
        mergeNbtTagInfo(nbtTagName, new CodecTagInfo(knownTypes.getFullyQualifiedType(codec), removeOptional(type), false, true));
    }

    private void parseCalleeStorer(String nbtTagName, String methodScope, String methodName, MethodCallExpr currNode) {
        if (EntityReference.class.getSimpleName().equals(methodScope)) {
            mergeNbtTagInfo(nbtTagName, new FuncTagInfo(methodScope, null, methodName, "EntityReference<?>", false, true));
        } else if (VariantUtils.class.getSimpleName().equals(methodScope)) {
            if (nbtTagName != null) {
                throw new AssertionError("Should be something like `VariantUtils.writeVariant(valueOutput, this.getVariant())`");
            }
            mergeNbtTagInfo(VariantUtils.TAG_VARIANT, new FuncTagInfo(methodScope, null, methodName, "Holder<?>", false, true));
        } else if (nbtTagName != null) {
            mergeNbtTagInfo(nbtTagName, new FuncTagInfo(methodScope, null, methodName, null, false, true));
        } else if (!(currNode.getScope().orElse(null) instanceof ThisExpr)) {
            throw new RuntimeException("Unknown tag: `" + currNode + "` in `" + currentMethod.getDeclarationAsString() + "`");
        }
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
            NbtTagInfo pk = pi.merge(pj);
            if (pk == null) {
                pk = pj.merge(pi);
            }
            if (pk == null) {
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
        if (!map.containsKey(pi)) {
            map.put(pi, pi);
        } else {
            NbtTagInfo pKey = null;
            NbtTagInfo pMerge = null;
            for (NbtTagInfo pj : map.values()) {
                pMerge = pi.merge(pj);
                if (pMerge == null) {
                    pMerge = pj.merge(pi);
                }
                if (pMerge != null) {
                    pKey = pj;
                    break;
                }
            }
            if (pMerge == null) {
                map.put(pi, pi);
            } else {
                map.remove(pKey);
                map.put(pMerge, pMerge);
            }
        }
    }

    public interface NbtTagInfo {
        NbtTagInfo merge(NbtTagInfo o);
        String typeString();
        boolean hasGetter();
        boolean hasPutter();
        boolean equals(Object that);
        int hashCode();
        String toString();

        static NbtTagInfo deserialize(String s) {
            return switch (s.charAt(0)) {
                case 'B' -> BuiltinTagInfo.deserialize(s);
                case 'C' -> CodecTagInfo.deserialize(s);
                case 'F' -> FuncTagInfo.deserialize(s);
                case 'U' -> UnknownTagInfo.deserialize(s);
                default -> throw new AssertionError("Bad NBT tag: " + s);
            };
        }
    }

    public record BuiltinTagInfo(TagMap type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
        private static final String DE_REGEX = """
                BuiltinTag\\{type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
        private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

        public static BuiltinTagInfo deserialize(String s) {
            Matcher matcher = DE_PATTERN.matcher(s);
            if (!matcher.find()) {
                return null;
            }
            TagMap type = TagMap.getByClazz(matcher.group(1));
            boolean hasGetter = Boolean.getBoolean(matcher.group(2));
            boolean hasPutter = Boolean.getBoolean(matcher.group(3));
            return new BuiltinTagInfo(type, hasGetter, hasPutter);
        }

        @Override
        public NbtTagInfo merge(NbtTagInfo o) {
            if (o instanceof UnknownTagInfo that) {
                return new BuiltinTagInfo(this.type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            if (o instanceof BuiltinTagInfo that) {
                TagMap type;
                if (this.type == that.type) {
                    type = this.type;
                } else if (this.type.isMorePreciseThan(that.type)) {
                    type = this.type;
                } else {
                    return null;
                }
                return new BuiltinTagInfo(type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            return null;
        }

        @Override
        public String typeString() {
            return type.getDataClass().getSimpleName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BuiltinTagInfo that) {
                return Objects.equals(this.type, that.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            return "BuiltinTag{" +
                    "type=\"" + typeString() + "\"" +
                    ", hasGetter=" + hasGetter +
                    ", hasPutter=" + hasPutter +
                    '}';
        }
    }

    public record CodecTagInfo(String codec, String type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
        private static final String DE_REGEX = """
                CodecTag\\{codec="([^"]+)", type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
        private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

        public static CodecTagInfo deserialize(String s) {
            Matcher matcher = DE_PATTERN.matcher(s);
            if (!matcher.find()) {
                return null;
            }
            String codec = matcher.group(1);
            String type = matcher.group(2);
            boolean hasGetter = Boolean.getBoolean(matcher.group(3));
            boolean hasPutter = Boolean.getBoolean(matcher.group(4));
            return new CodecTagInfo(type, codec, hasGetter, hasPutter);
        }

        @Override
        public NbtTagInfo merge(NbtTagInfo o) {
            if (o instanceof UnknownTagInfo that) {
                return new CodecTagInfo(this.codec, this.type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            if (o instanceof CodecTagInfo that && Objects.equals(this.codec, that.codec)) {
                String type;
                if (this.type == null) {
                    type = that.type;
                } else if (that.type == null) {
                    type = this.type;
                } else if (Objects.equals(this.type, that.type)){
                    type = this.type;
                } else {
                    return null;
                }
                return new CodecTagInfo(this.codec, type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            return null;
        }

        @Override
        public String typeString() {
            return type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CodecTagInfo that) {
                return Objects.equals(this.codec, that.codec) &&
                        Objects.equals(this.type, that.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(codec, type);
        }

        @Override
        public String toString() {
            return "CodecTag{" +
                    "codec=\"" + codec + "\"" +
                    ", type=\"" + type + "\"" +
                    ", hasGetter=" + hasGetter +
                    ", hasPutter=" + hasPutter +
                    '}';
        }
    }

    public record FuncTagInfo(String caller, String reader, String storer, String type, boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
        private static final String DE_REGEX = """
                FuncTag\\{caller="([^"]+)", reader="([^"]+)", storer="([^"]+)", type="([^"]+)", hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
        private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

        public static FuncTagInfo deserialize(String s) {
            Matcher matcher = DE_PATTERN.matcher(s);
            if (!matcher.find()) {
                return null;
            }
            String caller = matcher.group(1);
            String reader = matcher.group(2);
            String storer = matcher.group(3);
            String type = matcher.group(4);
            boolean hasGetter = Boolean.getBoolean(matcher.group(5));
            boolean hasPutter = Boolean.getBoolean(matcher.group(6));
            return new FuncTagInfo(type, caller, reader, storer, hasGetter, hasPutter);
        }

        @Override
        public NbtTagInfo merge(NbtTagInfo o) {
            if (o instanceof UnknownTagInfo that) {
                return new FuncTagInfo(this.caller, this.reader, this.storer, this.type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            if (o instanceof FuncTagInfo that && Objects.equals(this.caller, that.caller)) {
                String reader;
                if (this.reader == null) {
                    reader = that.reader;
                } else if (that.reader == null) {
                    reader = this.reader;
                } else if (Objects.equals(this.reader, that.reader)){
                    reader = this.reader;
                } else {
                    return null;
                }
                String storer;
                if (this.storer == null) {
                    storer = that.storer;
                } else if (that.storer == null) {
                    storer = this.storer;
                } else if (Objects.equals(this.storer, that.storer)){
                    storer = this.storer;
                } else {
                    return null;
                }
                String type;
                if (this.type == null) {
                    type = that.type;
                } else if (that.type == null) {
                    type = this.type;
                } else if (Objects.equals(this.type, that.type)){
                    type = this.type;
                } else {
                    return null;
                }
                return new FuncTagInfo(this.caller, reader, storer, type,
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            return null;
        }

        @Override
        public String typeString() {
            return type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FuncTagInfo that) {
                return Objects.equals(this.caller, that.caller) &&
                        Objects.equals(this.reader, that.reader) &&
                        Objects.equals(this.storer, that.storer) &&
                        Objects.equals(this.type, that.type);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(caller, reader, storer, type);
        }

        @Override
        public String toString() {
            return "FuncTag{" +
                    "caller=\"" + caller + "\"" +
                    ", reader=\"" + reader + "\"" +
                    ", storer=\"" + storer + "\"" +
                    ", type=\"" + type + "\"" +
                    ", hasGetter=" + hasGetter +
                    ", hasPutter=" + hasPutter +
                    '}';
        }
    }

    public record UnknownTagInfo(boolean hasGetter, boolean hasPutter) implements NbtTagInfo {
        private static final String DE_REGEX = """
                UnknownTagInfo\\{hasGetter=(true|false), hasPutter=(true|false)\\}
                """.replaceAll("[\r\n]", "");
        private static final Pattern DE_PATTERN = Pattern.compile(DE_REGEX);

        public static UnknownTagInfo deserialize(String s) {
            Matcher matcher = DE_PATTERN.matcher(s);
            if (!matcher.find()) {
                return null;
            }
            boolean hasGetter = Boolean.getBoolean(matcher.group(1));
            boolean hasPutter = Boolean.getBoolean(matcher.group(2));
            return new UnknownTagInfo(hasGetter, hasPutter);
        }

        @Override
        public NbtTagInfo merge(NbtTagInfo o) {
            if (o instanceof UnknownTagInfo that) {
                return new UnknownTagInfo(
                        this.hasGetter || that.hasGetter,
                        this.hasPutter || that.hasPutter);
            }
            return null;
        }

        @Override
        public String typeString() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UnknownTagInfo;
        }

        @Override
        public int hashCode() {
            return 114514;
        }

        @Override
        public String toString() {
            return "UnknownTag{" +
                    "hasGetter=" + hasGetter +
                    ", hasPutter=" + hasPutter +
                    '}';
        }
    }
}

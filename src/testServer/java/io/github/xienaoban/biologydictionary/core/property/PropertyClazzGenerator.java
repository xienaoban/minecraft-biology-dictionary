package io.github.xienaoban.biologydictionary.core.property;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import io.github.xienaoban.biologydictionary.core.EntityManager;
import io.github.xienaoban.biologydictionary.core.property.builtin.CodecProperty;
import io.github.xienaoban.biologydictionary.core.property.builtin.UnsupportedProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.EntityReferenceProperty;
import io.github.xienaoban.biologydictionary.core.property.vanilla.VariantProperty;
import io.github.xienaoban.biologydictionary.util.TestUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.variant.VariantUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class PropertyClazzGenerator {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Class<VanillaEntityProperties> TARGET_CLAZZ = VanillaEntityProperties.class;

    public static final String OUTPUT_CLAZZ_PACKAGE = TARGET_CLAZZ.getPackageName();
    private static final String PROPERTY_WRAPPER_CLAZZ_NAME = TARGET_CLAZZ.getSimpleName();

    public static final File OUTPUT_CLAZZ_DIR_PATH = new File(TestUtils.MAIN_JAVA_ROOT.toString(), OUTPUT_CLAZZ_PACKAGE.replaceAll("\\.", "/"));
    public static final File OUTPUT_CLAZZ_FILE_PATH = Paths.get(OUTPUT_CLAZZ_DIR_PATH.toString(), PROPERTY_WRAPPER_CLAZZ_NAME + ".java").toFile();

    private static final Type STRING_TYPE = new ClassOrInterfaceType(null, String.class.getSimpleName());
    private static final Type ENTITY_PROPERTY_TYPE = new ClassOrInterfaceType(null, new SimpleName(EntityProperty.class.getSimpleName()), new NodeList<>(new TypeParameter("?")));
    private static final Type MAP_STR_PROPERTY_TYPE = new ClassOrInterfaceType(null, new SimpleName(Map.class.getSimpleName()), new NodeList<>(STRING_TYPE, ENTITY_PROPERTY_TYPE));
    private static final Parameter MAP_STR_PROPERTY_PARAM = new Parameter(MAP_STR_PROPERTY_TYPE, "map");

    private static final Type ENTITY_PROPERTIES_TYPE = new ClassOrInterfaceType(null, new SimpleName(EntityProperties.class.getSimpleName()), new NodeList<>(new TypeParameter("?")));
    private static final Parameter ENTITY_PROPERTIES_PARAM = new Parameter(ENTITY_PROPERTIES_TYPE, "ep");

    private static final BlockStmt initMethodBlock = new BlockStmt();

    public static void generateAll() {
        CompilationUnit cu = getBaseCu();
        ClassOrInterfaceDeclaration wrapperClazz = cu.getClassByName(PROPERTY_WRAPPER_CLAZZ_NAME).orElseThrow();
        clearMembersToUpdate(wrapperClazz);
        addGeneralImports(cu);

        NbtTagCollector.loadAll();
        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            Class<? extends Entity> entityClazz = cur.getClazz();
            LOGGER.info("Testing {}", entityClazz);

            String sim = entityClazz.getSimpleName();
            initMethodBlock.addStatement("r(" + sim + ".class, new Of" + sim + "());");

            NbtTagCollector nbts = NbtTagCollector.get(entityClazz);
            PropertyClazzGenerator generator = new PropertyClazzGenerator(entityClazz, nbts, wrapperClazz);
            generator.generate();
            return true;
        });

        writeClassToFile(cu);
    }

    private static CompilationUnit getBaseCu() {
        try {
            String source = Files.readString(OUTPUT_CLAZZ_FILE_PATH.toPath());
            return AstParser.generateAst(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearMembersToUpdate(ClassOrInterfaceDeclaration wrapperClazz) {
        NodeList<BodyDeclaration<?>> list = wrapperClazz.getMembers();
        for (int i = 0; i < list.size(); ++i) {
            while (i < list.size()) {
                BodyDeclaration<?> body = list.get(i);
                if (body.isClassOrInterfaceDeclaration()) {
                    if (body.asClassOrInterfaceDeclaration().getNameAsString().startsWith("Of")) {
                        list.remove(i);
                        continue;
                    }
                } else if (body.isMethodDeclaration()) {
                    MethodDeclaration m = body.asMethodDeclaration();
                    if ("init".equals(m.getNameAsString())) {
                        m.setBody(initMethodBlock);
                    }
                }
                break;
            }
        }
    }

    private static void addGeneralImports(CompilationUnit cu) {
        cu.addImport(UnsupportedProperty.class.getPackageName(), false, true);
        cu.addImport(Map.class);
        cu.addImport(EntityProperty.class);

        ClassTypeCollector.loadImport();
        for (String s : ClassTypeCollector.getImports()) {
            cu.addImport(s);
        }
    }

    private static void writeClassToFile(CompilationUnit cu) {
        // if (Files.isRegularFile(path)) return; // do not overwrite
        try (PrintWriter out = new PrintWriter(PropertyClazzGenerator.OUTPUT_CLAZZ_FILE_PATH)) {
            out.print(cu.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<? extends Entity> entityClazz;
    private final NbtTagCollector nbts;

    private final CompilationUnit cu;
    private final ClassOrInterfaceDeclaration clazzAst;

    private final NodeList<Expression> creatorMethodBody = new NodeList<>(new NameExpr("map"));

    private PropertyClazzGenerator(Class<? extends Entity> entityClazz, NbtTagCollector nbts, ClassOrInterfaceDeclaration wrapperClazz) {
        String targetClazzSimpleName = "Of" + entityClazz.getSimpleName();
        this.entityClazz = entityClazz;
        this.nbts = nbts;
        this.cu = wrapperClazz.findCompilationUnit().orElseThrow();
        this.clazzAst = new ClassOrInterfaceDeclaration(
                NodeList.nodeList(Modifier.publicModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
                false, targetClazzSimpleName);
        this.clazzAst.addImplementedType(VanillaEntityProperties.Creator.class);
        wrapperClazz.addMember(this.clazzAst);
    }

    private void generate() {
        cu.addImport(entityClazz);
        addClazzComments();

        for (String propertyName : Stream.concat(nbts.getNbtTags().keySet().stream(), nbts.getConflicts().keySet().stream()).sorted().toList()) {
            addPropertyCreateAndGetMethods(propertyName, nbts.getNbtTags().getOrDefault(propertyName, null));
        }

        addPropertyCreatorMethod();
    }

    private void addClazzComments() {
        StringBuilder comment = new StringBuilder("This class is automatically generated by a script.\n");
        comment.append("Properties (NBT tags) of this entity:\n");
        for (var e : nbts.getNbtTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagInfo pi = e.getValue();
            comment.append(" - \"").append(e.getKey()).append("\": ").append(pi.typeString()).append('\n');
        }
        if (!nbts.getConflicts().isEmpty()) {
            comment.append("[Attention] Some properties cannot be recognized yet:\n");
            for (var e : nbts.getConflicts().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                Collection<NbtTagInfo> pis = e.getValue().values();
                comment.append(" - \"").append(e.getKey()).append("\": ").append(pis.stream().map(NbtTagInfo::typeString).toList()).append('\n');
            }
        }
        comment.append('\n');

        comment.append("@see ").append(entityClazz.getName());
        clazzAst.setJavadocComment(comment.toString());
    }

    private void addPropertyCreateAndGetMethods(String propertyName, NbtTagInfo propertyInfo) {
        String uc = toUpperCamelCase(propertyName);
        String returnRawType;
        String returnType;
        String arguments = "\"" + propertyName + "\"";
        switch (propertyInfo != null ? propertyInfo : new UnknownTagInfo(false, false)) {
            case BuiltinTagInfo builtinTagInfo:
                returnRawType = toUpperCamelCase(builtinTagInfo.typeString()) + "Property";
                returnType = returnRawType + "<" + entityClazz.getSimpleName() + ">";
                break;
            case CodecTagInfo codecTagInfo:
                returnRawType = CodecProperty.class.getSimpleName();
                returnType = returnRawType + "<" + entityClazz.getSimpleName() + ", " + codecTagInfo.typeString() + ">";
                arguments += ", " + removeGenerics(codecTagInfo.typeString()) + ".class, " + codecTagInfo.codec();
                break;
            case FuncTagInfo funcTagInfo:
                String caller = funcTagInfo.caller();
                if (EntityReference.class.getSimpleName().equals(caller)) {
                    returnRawType = EntityReferenceProperty.class.getSimpleName();
                    returnType = returnRawType + "<" + entityClazz.getSimpleName() + ">";
                } else if (VariantUtils.class.getSimpleName().equals(caller)) {
                    returnRawType = VariantProperty.class.getSimpleName();
                    returnType = returnRawType + "<" + entityClazz.getSimpleName() + ", " + funcTagInfo.typeString() + ">";
                    arguments = funcTagInfo.optional();
                } else {
                    returnRawType = UnsupportedProperty.class.getSimpleName();
                    returnType = returnRawType + "<" + entityClazz.getSimpleName() + ">";
                }
                break;
            default:
                returnRawType = UnsupportedProperty.class.getSimpleName();
                returnType = returnRawType + "<" + entityClazz.getSimpleName() + ">";
                break;
        }

        {
            String methodName = "create" + uc + "Property";
            MethodDeclaration method = clazzAst.addMethod(methodName, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
            try {
                method.setType(returnType);
            } catch (RuntimeException e) {
                LOGGER.error("Cannot parse `{}`", returnType);
                throw e;
            }
            method.setBody(new BlockStmt().addStatement("return new " + returnRawType + "<>(" + arguments + ");"));
        }

        {
            String methodName = "get" + uc + "Property";
            MethodDeclaration method = clazzAst.addMethod(methodName, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
            method.addParameter(ENTITY_PROPERTIES_PARAM);
            method.setType(returnType);
            method.setBody(new BlockStmt().addStatement("return g(ep, \"" + propertyName + "\");"));
        }

        creatorMethodBody.add(new MethodCallExpr("create" + uc + "Property"));
    }

    private void addPropertyCreatorMethod() {
        String methodName = "create";
        MethodDeclaration method = clazzAst.addMethod(methodName, Modifier.Keyword.PUBLIC);
        method.addParameter(MAP_STR_PROPERTY_PARAM);
        method.addAnnotation(new MarkerAnnotationExpr("Override"));
        method.setBody(new BlockStmt().addStatement(new ExpressionStmt(new MethodCallExpr(null, "p", creatorMethodBody))));
    }

    private static String toUpperCamelCase(String s) {
        StringBuilder sb = new StringBuilder();
        for (String sub : s.replaceAll("[^a-zA-Z0-9]+", "|").split("\\|")) {
            String sTmp = sub + "A";
            sb.append(Character.toUpperCase(sub.charAt(0)));
            for (int i = 1; i < sub.length(); ++i) {
                char pre = sTmp.charAt(i - 1);
                char cur = sTmp.charAt(i);
                char pro = sTmp.charAt(i + 1);
                if (Character.isUpperCase(pre) && Character.isUpperCase(pro)) {
                    sb.append(Character.toLowerCase(cur));
                } else {
                    sb.append(cur);
                }
            }
        }
        return sb.toString();
    }

    private static String toLowerCamelCase(String s) {
        s = toUpperCamelCase(s);
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String removeGenerics(String clazzName) {
        int i = clazzName.indexOf('<');
        if (i == -1) { return clazzName; }
        return clazzName.substring(0, i);
    }
}

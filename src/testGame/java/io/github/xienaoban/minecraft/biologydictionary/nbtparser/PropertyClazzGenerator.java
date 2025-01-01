package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.util.TestUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

/**
 * TODO: getter and setter and not contains exception
 */
public class PropertyClazzGenerator {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String OUTPUT_CLAZZ_PACKAGE = TranslationKeys.PACKAGE + ".core.property";
    public static final File OUTPUT_CLAZZ_PATH = new File(TestUtils.MAIN_JAVA_ROOT.toString(), OUTPUT_CLAZZ_PACKAGE.replaceAll("\\.", "/"));

    private static final String PROPERTY_CLAZZ_NAME = "EntityVanillaProperties";

    public static void generateAll() {
        File outputClassPath = Paths.get(OUTPUT_CLAZZ_PATH.toString(), PROPERTY_CLAZZ_NAME + ".java").toFile();
        CompilationUnit cu = new CompilationUnit(OUTPUT_CLAZZ_PACKAGE);
        ClassOrInterfaceDeclaration wrapperClazz = cu.addClass(PROPERTY_CLAZZ_NAME, Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL);
        addGeneralImports(cu);
        addGeneralAnnotations(wrapperClazz);

        EntityManager.getInstance().dfsEntityTree(false, (cur, depth) -> {
            Class<? extends Entity> entityClazz = cur.getClazz();
            // if (entityClazz != net.minecraft.world.entity.animal.armadillo.Armadillo.class) return true;
            LOGGER.info("Testing {}", entityClazz);
            NbtTagCollector nbts = NbtTagCollector.collect(entityClazz);
            PropertyClazzGenerator generator = new PropertyClazzGenerator(entityClazz, nbts, wrapperClazz);
            generator.generate();
            return true;
        });

        writeClassToFile(cu, outputClassPath);
    }

    private static void addGeneralImports(CompilationUnit cu) {
        cu.addImport("io.github.xienaoban.minecraft.biologydictionary.api.EntityVanillaProperty");
        cu.addImport("io.github.xienaoban.minecraft.biologydictionary.core.property.preset", false, true);
        cu.addImport(Environment.class);
        cu.addImport(EnvType.class);
        cu.addImport(Override.class);
        cu.addImport(CompoundTag.class);
        cu.addImport(Tag.class);
    }

    private static void addGeneralAnnotations(ClassOrInterfaceDeclaration wrapperClazz) {
        wrapperClazz.addAnnotation(new SingleMemberAnnotationExpr(new Name(Environment.class.getSimpleName()), new FieldAccessExpr(new NameExpr(EnvType.class.getSimpleName()), "CLIENT")));
    }

    private static void writeClassToFile(CompilationUnit cu, File outputClassPath) {
        // if (Files.isRegularFile(path)) return; // do not overwrite
        try (PrintWriter out = new PrintWriter(outputClassPath)) {
            out.print(cu.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<? extends Entity> entityClazz;
    private final NbtTagCollector nbts;

    private final CompilationUnit cu;
    private final ClassOrInterfaceDeclaration clazzAst;

    private PropertyClazzGenerator(Class<? extends Entity> entityClazz, NbtTagCollector nbts, ClassOrInterfaceDeclaration wrapperClazz) {
        String targetClazzSimpleName = "Of" + entityClazz.getSimpleName();
        this.entityClazz = entityClazz;
        this.nbts = nbts;
        this.cu = wrapperClazz.findCompilationUnit().orElseThrow();
        this.clazzAst = new ClassOrInterfaceDeclaration(
                NodeList.nodeList(Modifier.publicModifier(), Modifier.staticModifier(), Modifier.finalModifier()),
                false, targetClazzSimpleName);
        wrapperClazz.addMember(this.clazzAst);
    }

    private void generate() {
        cu.addImport(entityClazz);
        addClazzComments();

        for (var e : nbts.getNbtTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            addPropertyMethod(e.getKey(), e.getValue());
        }
    }

    private void addClazzComments() {
        StringBuilder comment = new StringBuilder("This class is automatically generated by a script.\n");
        comment.append("Properties (NBT tags) of this entity:\n");
        for (var e : nbts.getNbtTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagCollector.NbtTagInfo pi = e.getValue();
            comment.append(" - \"").append(e.getKey()).append("\": ").append(pi.getTypeString()).append('\n');
        }
        if (!nbts.getConflicts().isEmpty()) {
            comment.append("[Attention] Some properties cannot be recognized yet:\n");
            for (var e : nbts.getConflicts().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                Collection<NbtTagCollector.NbtTagInfo> pis = e.getValue().values();
                comment.append(" - \"").append(e.getKey()).append("\": ").append(pis.stream().sorted().map(NbtTagCollector.NbtTagInfo::getTypeString).toList()).append('\n');
            }
        }
        comment.append('\n');

        comment.append("@see ").append(entityClazz.getName());
        clazzAst.setJavadocComment(comment.toString());
    }

    private void addPropertyMethod(String propertyName, NbtTagCollector.NbtTagInfo propertyInfo) {
        String uc = toUpperCamelCase(propertyName);
        String returnType = toUpperCamelCase(propertyInfo.type().name()) + (propertyInfo.list() ? "List" : "") + "Property";

        String methodName = "create" + uc + "Property";
        MethodDeclaration method = clazzAst.addMethod(methodName, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
        method.setType(returnType);
        method.setBody(new BlockStmt().addStatement("return new " + returnType + "(\"" + propertyName + "\");"));
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
}

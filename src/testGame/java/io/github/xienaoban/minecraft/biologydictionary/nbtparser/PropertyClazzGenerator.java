package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import io.github.xienaoban.minecraft.biologydictionary.util.TestUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

/**
 * TODO: getter and setter and not contains exception
 */
public class PropertyClazzGenerator {
    public static final String OUTPUT_CLAZZ_PACKAGE = TranslationKeys.PACKAGE + ".core.property.vanilla";
    public static final File OUTPUT_CLAZZ_PATH = new File(TestUtils.MAIN_JAVA_ROOT.toString(), OUTPUT_CLAZZ_PACKAGE.replaceAll("\\.", "/"));

    private static final String PROPERTY_CLAZZ_NAME = "EntityVanillaProperty";
    private static final String PROPERTY_READ_METHOD_NAME = "readFrom";
    private static final String PROPERTY_WRITE_METHOD_NAME = "writeTo";
    private static final String TARGET_TAG_ARG = "vanillaNbt";

    private static final String PROPERTY_CLAZZ_TEMPLATE = """
            @EntityVanillaProperty.Property("{propertyName}")
            public static final class {clazzName} implements EntityVanillaProperty<{entityClazzName}> {
                private {typeClazzName} {memberName};
        
                @Override()
                public void readFrom(CompoundTag vanillaNbt) {
                    if (vanillaNbt.contains("{propertyName}", Tag.{tagName})) {
                        this.{memberName} = vanillaNbt.{tagGetterName}("{propertyName}");
                    }
                }
        
                @Override()
                public void writeTo(CompoundTag vanillaNbt) {
                    vanillaNbt.{tagPutterName}("{propertyName}", this.{memberName});
                }
            }
            """;

    private static final String PROPERTY_CLAZZ_LIST_TEMPLATE = """
            @EntityVanillaProperty.Property("{propertyName}")
            public static final class {clazzName} implements EntityVanillaProperty<{entityClazzName}> {
                private ArrayList<{typeClazzName}> {memberName} = new ArrayList<>();
        
                @Override()
                public void readFrom(CompoundTag vanillaNbt) {
                    if (vanillaNbt.contains("{propertyName}", Tag.TAG_LIST)) {
                        ListTag listTag = vanillaNbt.{tagGetterName}("{propertyName}", Tag.{tagName});
                        ArrayList<{typeClazzName}> list = new ArrayList<>();
                        for (int i = 0; i < listTag.size(); i++) {
                            list.add(listTag.{tagGetterName}(i));
                        }
                        Unsafe.storeFence();
                        this.{memberName} = list;
                    }
                }
        
                @Override()
                public void writeTo(CompoundTag vanillaNbt) {
                    ListTag listTag = new ListTag();
                    for ({typeClazzName} e : this.{memberName}) {
                        listTag.add({tagClazzName}.valueOf(e));
                    }
                    vanillaNbt.{tagPutterName}("{propertyName}", this.{memberName});
                }
            }
            """;

    public static void generate(Class<? extends Entity> entityClazz) {
        NbtTagCollector nbts = NbtTagCollector.collect(entityClazz);
        new PropertyClazzGenerator(entityClazz, nbts, OUTPUT_CLAZZ_PACKAGE, OUTPUT_CLAZZ_PATH).generate();
    }

    private final Class<? extends Entity> entityClazz;
    private final NbtTagCollector nbts;
    private final File targetFilePath;

    private final CompilationUnit cu;
    private final ClassOrInterfaceDeclaration wrapperClazz;

    private PropertyClazzGenerator(Class<? extends Entity> entityClazz, NbtTagCollector nbts, String targetPackage, File targetDirPath) {
        String targetClazzSimpleName = entityClazz.getSimpleName() + "Properties";
        this.entityClazz = entityClazz;
        this.nbts = nbts;
        this.targetFilePath = Paths.get(targetDirPath.toString(), targetClazzSimpleName + ".java").toFile();
        this.cu = new CompilationUnit(targetPackage);
        this.wrapperClazz = cu.addClass(targetClazzSimpleName, Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL);
    }

    private void generate() {
        addImports();
        addWrapperClazzComments();

        for (var e : nbts.getNbtTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            addPropertyClazz(e.getKey(), e.getValue());
        }

        writeClassToFile();
    }

    private void addImports() {
        cu.addImport("io.github.xienaoban.minecraft.biologydictionary.api.EntityVanillaProperty");
        cu.addImport(entityClazz);
        cu.addImport(Environment.class);
        cu.addImport(EnvType.class);
        cu.addImport(Override.class);
        cu.addImport(CompoundTag.class);
        cu.addImport(Tag.class);
    }

    private void addWrapperClazzComments() {
        StringBuilder comment = new StringBuilder("This class is automatically generated by the script. Please do not modify it.\n");
        comment.append("Properties (NBT tags) of this entity:\n");
        for (var e : nbts.getNbtTags().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            NbtTagCollector.NbtTagInfo pi = e.getValue();
            comment.append(" - \"").append(e.getKey()).append("\": ").append(pi.getTypeString()).append('\n');
        }
        if (!nbts.getConflicts().isEmpty()) {
            comment.append("[Attention] Some properties have more than 1 type! Such situation is not supported yet:\n");
            for (var e : nbts.getConflicts().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                Collection<NbtTagCollector.NbtTagInfo> pis = e.getValue().values();
                comment.append(" - \"").append(e.getKey()).append("\": ").append(pis.stream().sorted().map(NbtTagCollector.NbtTagInfo::getTypeString).toList());
            }
        }
        comment.append('\n');

        comment.append("@see ").append(entityClazz.getName());
        wrapperClazz.setJavadocComment(comment.toString());
        wrapperClazz.addAnnotation(new SingleMemberAnnotationExpr(new Name(Environment.class.getSimpleName()), new FieldAccessExpr(new NameExpr(EnvType.class.getSimpleName()), "CLIENT")));
    }

    private void addPropertyClazz(String propertyName, NbtTagCollector.NbtTagInfo propertyInfo) {
        boolean isList = propertyInfo.type() == TagMap.LIST;
        String clazzName = toUpperCamelCase(propertyName);
        String memberName = toLowerCamelCase(propertyName);

        String template = (isList ? PROPERTY_CLAZZ_LIST_TEMPLATE : PROPERTY_CLAZZ_TEMPLATE);
        String clazz = template
                .replace("{entityClazzName}", entityClazz.getSimpleName())
                .replace("{propertyName}", propertyName)
                .replace("{clazzName}", clazzName)
                .replace("{memberName}", memberName)
                .replace("{tagName}", propertyInfo.type().getIdName())
                .replace("{tagClazzName}", propertyInfo.type().getTagClazz().getSimpleName())
                .replace("{typeClazzName}", propertyInfo.type().getDataType().getSimpleName())
                .replace("{tagGetterName}", propertyInfo.type().getGetter())
                .replace("{tagPutterName}", propertyInfo.type().getPutter());

        clazz = "class UselessWrapper { " + clazz + " }";

        Optional<ClassOrInterfaceDeclaration> tmp = AstParser.generateAst(clazz).getClassByName("UselessWrapper");
        if (tmp.isEmpty()) throw new AssertionError(clazzName);
        ClassOrInterfaceDeclaration uselessWrapperClazz = tmp.get();
        ClassOrInterfaceDeclaration propertyClazz = uselessWrapperClazz.getMember(0).asClassOrInterfaceDeclaration();
        if (!clazzName.equals(propertyClazz.getNameAsString())) {
            throw new AssertionError(propertyClazz);
        }
        wrapperClazz.addMember(propertyClazz);

        if (isList) {
            cu.addImport(ArrayList.class);
            cu.addImport(propertyInfo.type().getTagClazz());
            cu.addImport(propertyInfo.type().getDataType());
            cu.addImport(Unsafe.class);
        }
        if (propertyInfo.type() == TagMap.UUID) {
            cu.addImport(UUID.class);
        }
    }

    private void writeClassToFile() {
        // if (Files.isRegularFile(path)) return; // do not overwrite
        try (PrintWriter out = new PrintWriter(targetFilePath)) {
            out.print(cu.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toUpperCamelCase(String s) {
        s = toCamelCase(s);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String toLowerCamelCase(String s) {
        s = toCamelCase(s);
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private static String toCamelCase(String s) {
        {
            s = s.replaceAll("[^a-zA-Z0-9]+", "|");
            StringBuilder sb = new StringBuilder();
            boolean upNext = false;
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (c == '|') {
                    upNext = true;
                } else {
                    sb.append(upNext ? Character.toUpperCase(c) : c);
                    upNext = false;
                }
            }
            s = sb.toString();
        }

        {
            String sTmp = s + "A";
            StringBuilder sb = new StringBuilder(s.substring(0, 1));
            for (int i = 1; i < s.length(); ++i) {
                char pre = sTmp.charAt(i - 1);
                char cur = sTmp.charAt(i);
                char pro = sTmp.charAt(i + 1);
                if (Character.isUpperCase(pre) && Character.isUpperCase(pro)) {
                    sb.append(Character.toLowerCase(cur));
                } else {
                    sb.append(cur);
                }
            }
            s = sb.toString();
        }
        return s;
    }
}

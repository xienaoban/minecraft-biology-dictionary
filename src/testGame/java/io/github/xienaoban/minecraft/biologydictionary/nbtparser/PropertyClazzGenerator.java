package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.xienaoban.minecraft.biologydictionary.util.TestUtils;
import io.github.xienaoban.minecraft.biologydictionary.util.TranslationKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.apache.commons.text.CaseUtils;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class PropertyClazzGenerator {
    private static final String OUTPUT_CLAZZ_PACKAGE = TranslationKeys.PACKAGE + ".core.property.vanilla";
    private static final File OUTPUT_CLAZZ_PATH = new File(TestUtils.MAIN_JAVA_ROOT.toString(), OUTPUT_CLAZZ_PACKAGE.replaceAll("\\.", "/"));

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

        for (var e : nbts.getNbtTags().entrySet()) {
            addPropertyClazz(e.getKey(), e.getValue());
        }

        writeClassToFile();
    }

    private void addImports() {
        cu.addImport("io.github.xienaoban.minecraft.biologydictionary.api.EntityVanillaProperty");
        cu.addImport(entityClazz);
        cu.addImport(Override.class);
        cu.addImport(CompoundTag.class);
        cu.addImport(Tag.class);
    }

    private void addWrapperClazzComments() {
        StringBuilder comment = new StringBuilder("This class is automatically generated by the script. Please do not modify it.\n");
        comment.append("Properties (NBT tags) of this entity:\n");
        for (var e : nbts.getNbtTags().entrySet()) {
            NbtTagCollector.NbtTagInfo pi = e.getValue();
            comment.append(" - \"").append(e.getKey()).append("\": ").append(pi).append('\n');
        }
        if (!nbts.getConflicts().isEmpty()) {
            comment.append("[Attention] Some properties have more than 1 type! Such situation is not supported yet:\n");
            for (var e : nbts.getConflicts().entrySet()) {
                Set<NbtTagCollector.NbtTagInfo> pis = e.getValue();
                comment.append(" - \"").append(e.getKey()).append("\": ").append(pis.stream().map(NbtTagCollector.NbtTagInfo::getTypeString).toList());
            }
        }
        comment.append('\n');

        comment.append("@see ").append(entityClazz.getName());
        wrapperClazz.setJavadocComment(comment.toString());
    }

    private void addPropertyClazz(String propertyName, NbtTagCollector.NbtTagInfo propertyInfo) {
        boolean isList = propertyInfo.type() == TagMap.LIST;
        // TODO!!!! CanDuplicate -> Canduplicate
        String clazzName = CaseUtils.toCamelCase(propertyName, true, '_');
        String memberName = CaseUtils.toCamelCase(propertyName, false, '_');

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

        Optional<ClassOrInterfaceDeclaration> tmp = AstParser.generateAst(clazz).getClassByName(clazzName);
        if (tmp.isEmpty()) throw new AssertionError();
        ClassOrInterfaceDeclaration propertyClazz = tmp.get();
        wrapperClazz.addMember(propertyClazz);

        if (isList) {
            cu.addImport(ArrayList.class);
            cu.addImport(propertyInfo.type().getTagClazz());
            cu.addImport(propertyInfo.type().getDataType());
            cu.addImport(Unsafe.class);
        }

        // propertyClazz.addImplementedType(PROPERTY_CLAZZ_NAME + "<" + entityClazz.getSimpleName() + ">");
        // propertyClazz.addAnnotation(new SingleMemberAnnotationExpr(new Name("EntityVanillaProperty.Property"), new StringLiteralExpr(propertyName)));
        //
        // propertyClazz.addField(propertyInfo.type().getDataType(), memberName, Modifier.Keyword.PRIVATE);
        //
        // MethodDeclaration mRead = propertyClazz.addMethod(PROPERTY_READ_METHOD_NAME, Modifier.Keyword.PUBLIC);
        // mRead.addAnnotation(Override.class);
        // mRead.addParameter(CompoundTag.class, TARGET_TAG_ARG);
        //
        //
        // mRead.setBody(new BlockStmt());
        //
        // MethodDeclaration mWrite = propertyClazz.addMethod(PROPERTY_WRITE_METHOD_NAME, Modifier.Keyword.PUBLIC);
        // mWrite.addAnnotation(Override.class);
        // mWrite.addParameter(CompoundTag.class, TARGET_TAG_ARG);
    }

    private void writeClassToFile() {
        // if (Files.isRegularFile(path)) return; // do not overwrite
        try (PrintWriter out = new PrintWriter(targetFilePath)) {
            out.print(cu.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

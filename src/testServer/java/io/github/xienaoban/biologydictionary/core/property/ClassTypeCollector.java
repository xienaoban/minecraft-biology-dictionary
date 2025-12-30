package io.github.xienaoban.biologydictionary.core.property;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ClassTypeCollector extends AbstractVisitorWrapper<Void> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path IMPORT_PATH = Path.of(PropertyClazzGenerator.OUTPUT_CLAZZ_DIR_PATH.toString(), ".nbt-tag-import.log");

    private static final Map<String, String> toImports = new HashMap<>();

    public static void loadImport() {
        try (BufferedReader fileReader = Files.newBufferedReader(IMPORT_PATH)) {
            fileReader.lines().forEach(s -> addImport(s.substring(s.lastIndexOf('.')), s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Collection<String> getImports() {
        return toImports.values();
    }

    public static void storeImport() {
        try (BufferedWriter fileWriter = Files.newBufferedWriter(IMPORT_PATH)) {
            for (String s : toImports.values().stream().sorted().toList()) {
                fileWriter.write(s);
                fileWriter.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addImport(String simpleName, String fullName) {
        if (toImports.containsKey(simpleName)) {
            String oldFullName = toImports.get(simpleName);
            if (!Objects.equals(fullName, oldFullName)) {
                LOGGER.warn("Duplicated imported class: key=`{}`, old-value=`{}`, new-value=`{}`", simpleName, oldFullName, fullName);
            }
        } else {
            toImports.put(simpleName, fullName);
        }
    }

    private String currPackageName = null;
    private String currClazzName = null;
    /**
     * K: Type Name, V: Fully Qualified Type Name
     */
    private final Map<String, String> fullyQualifiedTypes = new HashMap<>();

    /**
     * K: Field Name, V: Field Type Name
     */
    private final Map<String, String> fieldTypes = new HashMap<>();

    /**
     * K: Method Name, V: Method Return Type Name & Argument Type Names
     */
    private final Map<String, MethodTypes> methodTypes = new HashMap<>();

    public record MethodTypes(String returnType, List<String> argumentTypes) {}

    public ClassTypeCollector(Class<? extends Entity> entityClazz) {
        addImport(entityClazz.getSimpleName(), entityClazz.getName());

        fullyQualifiedTypes.put("this", "this");
        fullyQualifiedTypes.put("super", "super");
        fullyQualifiedTypes.put("extends", "extends");
        fullyQualifiedTypes.put("Object", "Object");
        fullyQualifiedTypes.put("String", "String");
    }

    public String getFullyQualifiedType(String type) {
        if (type == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        StringBuilder currType = new StringBuilder();
        boolean skipChars = false;
        for (int i = 0; i < type.length(); ++i) {
            char c = type.charAt(i);
            if (c == '.' || c == ':' || c == '<' || c == '>' || c == ',' || c == ' ' || c == '(' || c == ')' || c == '?') {
                skipChars = (c == '.' || c == ':');
                if (!currType.isEmpty()) {
                    res.append(getFullyQualifiedType0(currType.toString()));
                    currType = new StringBuilder();
                }
                res.append(c);
            } else if (Character.isLetterOrDigit(c) || c == '_') {
                if (skipChars) { res.append(c); }
                else { currType.append(c); }
            } else {
                throw new AssertionError(type + " -> " + c);
            }
        }
        if (!currType.isEmpty()) {
            res.append(getFullyQualifiedType0(currType.toString()));
        }
        return res.toString();
    }

    private String getFullyQualifiedType0(String type) {
        return fullyQualifiedTypes.computeIfAbsent(type, t -> {
            addImport(t, currPackageName + '.' + t);
            return t;
        });
    }

    public String getFieldType(String name) {
        return getFullyQualifiedType(fieldTypes.get(name));
    }

    public String getMethodRetType(String name) {
        MethodTypes mt = methodTypes.get(name);
        if (mt == null) { return null; }
        return getFullyQualifiedType(mt.returnType());
    }

    public String getMethodArgType(String name, int argIdx) {
        MethodTypes mt = methodTypes.get(name);
        if (mt == null) { return null; }
        return getFullyQualifiedType(mt.argumentTypes().get(argIdx));
    }

    @Override
    public void visit(PackageDeclaration n, Void arg) {
        currPackageName = n.getNameAsString();
        super.visit(n, arg);
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        String type = n.getName().getIdentifier();
        String fullyQualifiedType = n.getNameAsString();
        if (fullyQualifiedTypes.containsKey(type)) {
            throw new RuntimeException("Duplicated fields? Field: \"" + n + "\".");
        }
        // If it can be imported, then it is a general class that does not require a
        // fully qualified name.
        // fullyQualifiedTypes.put(type, fullyQualifiedType);
        fullyQualifiedTypes.put(type, type);
        addImport(type, fullyQualifiedType);
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, Void arg) {
        for (VariableDeclarator vd : n.getVariables()) {
            String name = vd.getNameAsString();
            String type = vd.getTypeAsString();
            if (fieldTypes.containsKey(name)) {
                throw new RuntimeException("Duplicated fields? name=\"" + name + "\", "
                        + "old-type=\"" + fieldTypes.get(name) + "\", "
                        + "new-type=\"" + type + "\", field: \"" + n + "\".");
            }
            fieldTypes.put(name, type);
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        String name = n.getNameAsString();
        String retType = n.getTypeAsString();
        List<String> argTypes = n.getParameters().stream().map(p -> p.getType().asString()).toList();
        if (methodTypes.containsKey(name)) {
            methodTypes.put(name, new MethodTypes("DUP", Collections.emptyList()));
        }
        methodTypes.put(name, new MethodTypes(retType, argTypes));
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        if (visitClazzDeclaration(n)) {
            super.visit(n, arg);
        }
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        if (visitClazzDeclaration(n)) {
            super.visit(n, arg);
        }
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {
        if (visitClazzDeclaration(n)) {
            super.visit(n, arg);
        }
    }

    /**
     * @return Should invoke super or not
     */
    private boolean visitClazzDeclaration(TypeDeclaration<?> n) {
        if (depth == 1 && n.hasModifier(Modifier.Keyword.PUBLIC)) {
            currClazzName = n.getNameAsString();
            return true;
        } else {
            String innerClazzName = n.getNameAsString();
            if (fullyQualifiedTypes.containsKey(innerClazzName)) {
                throw new RuntimeException("Duplicated fields? Field: \"" + n + "\".");
            }
            // No need to use fully qualified name for inner classes.
            // fullyQualifiedTypes.put(innerClazzName, currPackageName + '.' + currClazzName + '.' + innerClazzName);
            fullyQualifiedTypes.put(innerClazzName, currClazzName + '.' + innerClazzName);
            return false;
        }
    }
}

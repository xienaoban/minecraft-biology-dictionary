package io.github.xienaoban.minecraft.biologydictionary.core.property;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import net.minecraft.world.entity.Entity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ClassTypeCollector extends AbstractVisitorWrapper<Void> {
    private static final Path LOGGER_PATH = Path.of(PropertyClazzGenerator.OUTPUT_CLAZZ_DIR_PATH.toString(), ".entity-types.log");
    private static final BufferedWriter nbtFileWriter;

    static {
        try {
            nbtFileWriter = Files.newBufferedWriter(LOGGER_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(String line) {
        try {
            nbtFileWriter.write(line);
            nbtFileWriter.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        write("Types of entity class " + entityClazz);
    }

    private String getFullyQualifiedType(String type) {
        if (type == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        StringBuilder currType = new StringBuilder();
        boolean skipChars = false;
        for (int i = 0; i < type.length(); ++i) {
            char c = type.charAt(i);
            if (c == '<' || c == '>' || c == ',' || c == ' ' || c == '.') {
                skipChars = (c == '.');
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
        return fullyQualifiedTypes.getOrDefault(type, currPackageName + '.' + type);
    }

    public String getFieldType(String name) {
        return getFullyQualifiedType(fieldTypes.get(name));
    }

    public String getMethodRetType(String name) {
        return getFullyQualifiedType(methodTypes.get(name).returnType());
    }

    public String getMethodArgType(String name, int argIdx) {
        return getFullyQualifiedType(methodTypes.get(name).argumentTypes().get(argIdx));
    }

    public void print() {
        write("P: " + currPackageName);
        write("C: " + currClazzName);
        for (var e : fullyQualifiedTypes.entrySet()) {
            write("Q: " + e.getKey() + ": " + e.getValue());
        }
        for (var e : fieldTypes.entrySet()) {
            write("F: " + e.getKey() + ": " + e.getValue());
        }
        for (var e : methodTypes.entrySet()) {
            write("M: " + e.getKey() + ": " + e.getValue());
        }
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
        // fully qualified name "fullyQualifiedTypes.put(type, fullyQualifiedType);".
        fullyQualifiedTypes.put(type, type);
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, Void arg) {
        for (VariableDeclarator vd : n.getVariables()) {
            String name = vd.getNameAsString();
            String type = vd.getTypeAsString();
            if (fieldTypes.containsKey(name)) {
                throw new RuntimeException("Duplicated fields? name=\"" + name + "\", old-type=\"" + fieldTypes.get(name) + "\", new-type=\"" + type + "\", field: \"" + n + "\".");
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
        if (depth == 1 && n.hasModifier(Modifier.Keyword.PUBLIC)) {
            currClazzName = n.getNameAsString();
            super.visit(n, arg);
        } else {
            String innerClazzName = n.getNameAsString();
            if (fullyQualifiedTypes.containsKey(innerClazzName)) {
                throw new RuntimeException("Duplicated fields? Field: \"" + n + "\".");
            }
            fullyQualifiedTypes.put(innerClazzName, currPackageName + '.' + currClazzName + '.' + innerClazzName);
        }
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {}
}

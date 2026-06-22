package io.github.xienaoban.biologydictionary.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class CheckClientOnlyTask extends DefaultTask {
    private static final String CLIENT_ONLY_DESC = "Lio/github/xienaoban/biologydictionary/platform/ClientOnly;";
    private static final String CLIENT_AND_SERVER_DESC = "Lio/github/xienaoban/biologydictionary/platform/ClientAndServer;";

    private final DirectoryProperty classesDir;
    private final ConfigurableFileCollection classpath;

    @Inject
    public CheckClientOnlyTask(ObjectFactory objects) {
        this.classesDir = objects.directoryProperty();
        this.classpath = objects.fileCollection();
    }

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public DirectoryProperty getClassesDir() {
        return classesDir;
    }

    @Classpath
    public ConfigurableFileCollection getClasspath() {
        return classpath;
    }

    @TaskAction
    public void run() throws IOException {
        Path root = classesDir.get().getAsFile().toPath();
        List<byte[]> projectClasses = loadProjectClasses(root);
        URL[] urls = classpath.getFiles().stream()
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .toArray(URL[]::new);

        try (URLClassLoader loader = new URLClassLoader(urls, CheckClientOnlyTask.class.getClassLoader())) {
            List<String> violations = check(projectClasses, loader);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder("@ClientOnly check FAILED (" + violations.size() + " violations):\n");
                for (String violation : violations) {
                    sb.append("  ").append(violation).append('\n');
                }
                throw new GradleException(sb.toString());
            }
        }
    }

    private static List<String> check(List<byte[]> projectClasses, ClassLoader cl) {
        List<ClassInfo> allInfos = projectClasses.stream()
                .map(CheckClientOnlyTask::analyzeProjectClass)
                .filter(Objects::nonNull)
                .toList();

        Set<String> clientOnlyClassNames = new HashSet<>();
        Set<MethodRef> clientOnlyMethodRefs = new HashSet<>();
        for (ClassInfo info : allInfos) {
            if (info.isClientOnly) clientOnlyClassNames.add(info.internalName);
            for (MethodInfo method : info.methods) {
                if (method.isClientOnly) {
                    clientOnlyMethodRefs.add(new MethodRef(info.internalName, method.name, method.descriptor));
                }
            }
        }

        for (ClassInfo info : allInfos) {
            if (clientOnlyClassNames.contains(info.internalName)) continue;
            String enclosing = info.internalName;
            while (enclosing.contains("$")) {
                enclosing = enclosing.substring(0, enclosing.lastIndexOf('$'));
                if (clientOnlyClassNames.contains(enclosing)) {
                    clientOnlyClassNames.add(info.internalName);
                    break;
                }
            }
        }

        Set<String> violations = new TreeSet<>();
        Map<String, McClassInfo> mcCache = new HashMap<>();

        for (ClassInfo info : allInfos) {
            if (clientOnlyClassNames.contains(info.internalName)) continue;

            String className = info.internalName.replace('/', '.');
            for (String ref : info.classRefs) {
                checkTypeRef(ref, className, null, true, clientOnlyClassNames, mcCache, cl, violations);
            }

            for (MethodInfo method : info.methods) {
                boolean checkProjectClientOnly = !method.isClientOnly && !method.isClientAndServer;

                for (String ref : method.typeRefs) {
                    checkTypeRef(ref, className, method.name, checkProjectClientOnly, clientOnlyClassNames, mcCache, cl, violations);
                }

                for (MethodRef call : method.methodCalls) {
                    String entry = className + "." + method.name + " -> " + call.owner.replace('/', '.') + "." + call.name;
                    if (checkProjectClientOnly) {
                        if (clientOnlyClassNames.contains(call.owner)) {
                            violations.add(entry);
                            continue;
                        }
                        if (clientOnlyMethodRefs.contains(call)) {
                            violations.add(entry);
                            continue;
                        }
                    }

                    McClassInfo mcInfo = mcCache.computeIfAbsent(call.owner, k -> loadMcClassInfo(k, cl));
                    if (mcInfo.isClientClass) {
                        violations.add(entry);
                        continue;
                    }
                    if (mcInfo.clientMethods.contains(call)) {
                        violations.add(entry);
                    }
                }
            }
        }

        return new ArrayList<>(violations);
    }

    private static void checkTypeRef(String ref, String className, String methodName,
                                     boolean checkProjectClientOnly,
                                     Set<String> clientOnlyClassNames,
                                     Map<String, McClassInfo> mcCache, ClassLoader cl,
                                     Set<String> violations) {
        String label = methodName != null ? className + "." + methodName : className;
        String target = ref.replace('/', '.');
        if (checkProjectClientOnly && clientOnlyClassNames.contains(ref)) {
            violations.add(label + " -> " + target);
            return;
        }
        McClassInfo mcInfo = mcCache.computeIfAbsent(ref, k -> loadMcClassInfo(k, cl));
        if (mcInfo.isClientClass) violations.add(label + " -> " + target);
    }

    private static List<byte[]> loadProjectClasses(Path root) throws IOException {
        List<byte[]> result = new ArrayList<>();
        if (!Files.isDirectory(root)) return result;

        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            result.add(Files.readAllBytes(path));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return result;
    }

    private static ClassInfo analyzeProjectClass(byte[] bytes) {
        String[] internalName = {null};
        boolean[] isClientOnly = {false};
        Set<String> classRefs = new HashSet<>();
        List<MethodInfo> methods = new ArrayList<>();

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                internalName[0] = name;
                if (superName != null) classRefs.add(superName);
                if (interfaces != null) {
                    for (String iface : interfaces) classRefs.add(iface);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (CLIENT_ONLY_DESC.equals(descriptor)) {
                    isClientOnly[0] = true;
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                extractRefsFromDescriptor(descriptor, classRefs);
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                Set<String> methodTypeRefs = new HashSet<>();
                Set<MethodRef> methodCalls = new HashSet<>();
                boolean[] methodIsClientOnly = {false};
                boolean[] methodIsClientAndServer = {false};

                extractRefsFromDescriptor(descriptor, methodTypeRefs);

                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        if (CLIENT_ONLY_DESC.equals(desc)) {
                            methodIsClientOnly[0] = true;
                        } else if (CLIENT_AND_SERVER_DESC.equals(desc)) {
                            methodIsClientAndServer[0] = true;
                        }
                        return super.visitAnnotation(desc, visible);
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        methodTypeRefs.add(type);
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        methodTypeRefs.add(owner);
                        extractRefsFromDescriptor(desc, methodTypeRefs);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                        methodTypeRefs.add(owner);
                        extractRefsFromDescriptor(desc, methodTypeRefs);
                        methodCalls.add(new MethodRef(owner, name, desc));
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof Type t && t.getSort() == Type.OBJECT) {
                            methodTypeRefs.add(t.getInternalName());
                        }
                    }

                    @Override
                    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                        if (type != null) methodTypeRefs.add(type);
                    }

                    @Override
                    public void visitLocalVariable(String name, String desc, String sig, Label start, Label end, int index) {
                        extractRefsFromDescriptor(desc, methodTypeRefs);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                        extractRefsFromDescriptor(desc, methodTypeRefs);
                    }

                    @Override
                    public void visitEnd() {
                        methods.add(new MethodInfo(name, descriptor, methodIsClientOnly[0],
                                methodIsClientAndServer[0], methodTypeRefs, methodCalls));
                    }
                };
            }
        };

        new ClassReader(bytes).accept(cv, 0);
        return new ClassInfo(internalName[0], isClientOnly[0], classRefs, methods);
    }

    private static McClassInfo loadMcClassInfo(String internalName, ClassLoader cl) {
        String path = internalName + ".class";
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) return new McClassInfo(false, Set.of());
            byte[] bytes = is.readAllBytes();
            boolean[] isClientClass = {false};
            Set<MethodRef> clientMethods = new HashSet<>();
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if ("Lnet/fabricmc/api/Environment;".equals(descriptor)) {
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitEnum(String name, String descriptor, String value) {
                                if ("CLIENT".equals(value)) isClientClass[0] = true;
                            }
                        };
                    }
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    boolean[] methodIsClient = {false};
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            if ("Lnet/fabricmc/api/Environment;".equals(desc)) {
                                return new AnnotationVisitor(Opcodes.ASM9) {
                                    @Override
                                    public void visitEnum(String name, String descriptor, String value) {
                                        if ("CLIENT".equals(value)) methodIsClient[0] = true;
                                    }
                                };
                            }
                            return null;
                        }

                        @Override
                        public void visitEnd() {
                            if (methodIsClient[0]) clientMethods.add(new MethodRef(internalName, name, descriptor));
                        }
                    };
                }
            };
            new ClassReader(bytes).accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return new McClassInfo(isClientClass[0], clientMethods);
        } catch (IOException e) {
            return new McClassInfo(false, Set.of());
        }
    }

    private static void extractRefsFromDescriptor(String descriptor, Set<String> refs) {
        Type type = Type.getType(descriptor);
        if (type.getSort() == Type.METHOD) {
            for (Type t : type.getArgumentTypes()) {
                collectTypeRefs(t, refs);
            }
            collectTypeRefs(type.getReturnType(), refs);
        } else {
            collectTypeRefs(type, refs);
        }
    }

    private static void collectTypeRefs(Type type, Set<String> refs) {
        if (type.getSort() == Type.ARRAY) type = type.getElementType();
        if (type.getSort() == Type.OBJECT) refs.add(type.getInternalName());
    }

    private record MethodRef(String owner, String name, String descriptor) {}
    private record McClassInfo(boolean isClientClass, Set<MethodRef> clientMethods) {}
    private record MethodInfo(String name, String descriptor, boolean isClientOnly,
                              boolean isClientAndServer, Set<String> typeRefs,
                              Set<MethodRef> methodCalls) {}
    private record ClassInfo(String internalName, boolean isClientOnly,
                             Set<String> classRefs, List<MethodInfo> methods) {}
}

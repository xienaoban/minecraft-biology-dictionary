package io.github.xienaoban.biologydictionary;

import net.minecraft.gametest.framework.GameTestHelper;
import org.objectweb.asm.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ClientOnlyCheckTest {
    public void testClientOnlyCheck(GameTestHelper helper) {
        try {
            List<String> violations = ClientOnlyCheck.check();
            if (violations.isEmpty()) {
                helper.succeed();
            } else {
                StringBuilder sb = new StringBuilder("@ClientOnly check FAILED (" + violations.size() + " violations):\n");
                for (String v : violations) {
                    sb.append("  ").append(v).append("\n");
                }
                helper.fail(sb.toString());
            }
        } catch (Throwable e) {
            helper.fail("@ClientOnly check error: " + e.getMessage());
        }
    }
}

final class ClientOnlyCheck {
    private static final String CLIENT_ONLY_DESC = "Lio/github/xienaoban/biologydictionary/platform/ClientOnly;";

    public static List<String> check() throws Exception {
        List<byte[]> projectClasses = loadProjectClasses();
        ClassLoader cl = ClientOnlyCheck.class.getClassLoader();

        List<ClassInfo> allInfos = projectClasses.stream()
                .map(ClientOnlyCheck::analyzeProjectClass)
                .filter(Objects::nonNull)
                .toList();

        Set<String> clientOnlyNames = new HashSet<>();
        for (ClassInfo info : allInfos) {
            if (info.isClientOnly) clientOnlyNames.add(info.internalName);
        }

        List<Violation> violations = new ArrayList<>();
        Map<String, Boolean> mcClientCache = new HashMap<>();

        for (ClassInfo info : allInfos) {
            if (info.isClientOnly) continue;
            String outerName = info.internalName.contains("$")
                    ? info.internalName.substring(0, info.internalName.indexOf('$')) : null;
            if (outerName != null && clientOnlyNames.contains(outerName)) continue;
            for (String ref : info.references) {
                if (!ref.startsWith("net/minecraft/client/")) continue;
                Boolean isClient = mcClientCache.get(ref);
                if (isClient == null) {
                    isClient = checkMcClientAnnotation(ref, cl);
                    mcClientCache.put(ref, isClient);
                }
                if (isClient) {
                    violations.add(new Violation(
                            info.internalName.replace('/', '.'),
                            ref.replace('/', '.')
                    ));
                }
            }
        }

        violations.sort(Comparator.comparing(v -> v.projectClass));
        List<String> result = new ArrayList<>();
        for (Violation v : violations) {
            result.add(v.projectClass + " -> " + v.mcClass);
        }
        return result;
    }

    private static List<byte[]> loadProjectClasses() throws IOException, URISyntaxException {
        URL location = io.github.xienaoban.biologydictionary.platform.ClientOnly.class
                .getProtectionDomain().getCodeSource().getLocation();
        Path path = Paths.get(location.toURI());
        List<byte[]> result = new ArrayList<>();

        if (Files.isDirectory(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.filter(p -> p.toString().endsWith(".class"))
                        .forEach(p -> {
                            try { result.add(Files.readAllBytes(p)); }
                            catch (IOException e) { throw new UncheckedIOException(e); }
                        });
            }
        } else {
            try (JarFile jar = new JarFile(path.toFile())) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        try (InputStream is = jar.getInputStream(entry)) {
                            result.add(is.readAllBytes());
                        }
                    }
                }
            }
        }
        return result;
    }

    private static ClassInfo analyzeProjectClass(byte[] bytes) {
        String[] internalName = {null};
        boolean[] isClientOnly = {false};
        Set<String> refs = new HashSet<>();

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                internalName[0] = name;
                if (superName != null) refs.add(superName);
                if (interfaces != null) {
                    for (String iface : interfaces) refs.add(iface);
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
                extractRefsFromDescriptor(descriptor, refs);
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                extractRefsFromDescriptor(descriptor, refs);
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        refs.add(type);
                    }

                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        refs.add(owner);
                        extractRefsFromDescriptor(descriptor, refs);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        refs.add(owner);
                        extractRefsFromDescriptor(descriptor, refs);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof Type t && t.getSort() == Type.OBJECT) {
                            refs.add(t.getInternalName());
                        }
                    }

                    @Override
                    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                        if (type != null) refs.add(type);
                    }

                    @Override
                    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        extractRefsFromDescriptor(descriptor, refs);
                    }

                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                        extractRefsFromDescriptor(descriptor, refs);
                    }
                };
            }
        };

        ClassReader cr = new ClassReader(bytes);
        cr.accept(cv, 0);
        return new ClassInfo(internalName[0], isClientOnly[0], refs);
    }

    private static boolean checkMcClientAnnotation(String internalName, ClassLoader cl) {
        String path = internalName + ".class";
        try (InputStream is = cl.getResourceAsStream(path)) {
            if (is == null) return false;
            byte[] bytes = is.readAllBytes();
            boolean[] isClient = {false};
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if ("Lnet/fabricmc/api/Environment;".equals(descriptor)) {
                        return new AnnotationVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitEnum(String name, String descriptor, String value) {
                                if ("CLIENT".equals(value)) isClient[0] = true;
                            }
                        };
                    }
                    return null;
                }
            };
            new ClassReader(bytes).accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return isClient[0];
        } catch (IOException e) {
            return false;
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

    private record ClassInfo(String internalName, boolean isClientOnly, Set<String> references) {}
    private record Violation(String projectClass, String mcClass) {}
}
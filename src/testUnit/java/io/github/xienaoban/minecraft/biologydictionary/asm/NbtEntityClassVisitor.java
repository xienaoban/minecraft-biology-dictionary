package io.github.xienaoban.minecraft.biologydictionary.asm;

import io.github.xienaoban.minecraft.biologydictionary.platform.access.EntityApi;
import io.github.xienaoban.minecraft.biologydictionary.platform.util.JavaNames;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.*;


public class NbtEntityClassVisitor extends ClassVisitor {

    private static ClassWriter createClassWriter() {
        ClassWriter classWriter = new ClassWriter(0);
        String className = EntityApi.class.getName()
                .replace(EntityApi.class.getSimpleName(), "EntityNbtApi")
                .replace('.', '/');
        classWriter.visit(V17, ACC_PUBLIC | ACC_FINAL,
                className, null, null, null);
        return classWriter;
    }

    private final ClassWriter classWriter;

    public NbtEntityClassVisitor() {
        super(Opcodes.ASM9);
        this.classWriter = createClassWriter();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        return switch (name) {
            case JavaNames.ENTITY_READ_MORE_NBT -> new NbtReaderMethodVisitor(classWriter, mv);
            case JavaNames.ENTITY_WRITE_MORE_NBT -> new NbtWriterMethodVisitor(classWriter, mv);
            default -> mv;
        };
    }
}

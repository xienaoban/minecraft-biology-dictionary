package io.github.xienaoban.minecraft.biologydictionary.asm;

import io.github.xienaoban.minecraft.biologydictionary.platform.util.JavaNames;
import net.minecraft.nbt.CompoundTag;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class NbtEntityClassVisitor extends ClassVisitor {
    public static final String COMPOUND_TAG_NAME = CompoundTag.class.getName().replace('.', '/');
    public static final String COMPOUND_TAG_SIGNATURE = "(L" + COMPOUND_TAG_NAME + ";)V";

    private final ClassWriter classWriter;

    public NbtEntityClassVisitor(ClassWriter classWriter) {
        super(Opcodes.ASM9);
        this.classWriter = classWriter;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        return switch (name) {
            case JavaNames.ENTITY_WRITE_MORE_NBT -> new NbtWriterMethodVisitor(classWriter, mv);
            case JavaNames.ENTITY_READ_MORE_NBT -> new NbtReaderMethodVisitor(classWriter, mv);
            default -> mv;
        };
    }
}

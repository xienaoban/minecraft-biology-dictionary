package io.github.xienaoban.minecraft.biologydictionary.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class NbtReaderMethodVisitor extends MethodVisitor {
    private final ClassWriter classWriter;

    public NbtReaderMethodVisitor(ClassWriter classWriter, MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
        this.classWriter = classWriter;
    }
}

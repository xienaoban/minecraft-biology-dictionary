package io.github.xienaoban.minecraft.biologydictionary.asm;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface MethodBytecodeWriter {
    void accept(MethodVisitor methodVisitor);
}

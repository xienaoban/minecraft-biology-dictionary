package io.github.xienaoban.minecraft.biologydictionary.asm;

import net.minecraft.nbt.CompoundTag;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class NbtReaderMethodVisitor extends MethodVisitor {
    private final ClassWriter classWriter;

    public NbtReaderMethodVisitor(ClassWriter classWriter, MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
        this.classWriter = classWriter;
        if (super.mv != null) {
            throw new RuntimeException("mv should be null");
        }
    }

    @Override
    public void visitParameter(String name, int access) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitCode() {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitInsn(int opcode) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
        System.out.println("Method: " + Printer.OPCODES[opcode] + ", " + owner + ", " + name + ", " + descriptor + ", " + isInterface);
        if (opcode == INVOKEVIRTUAL && owner.equals(CompoundTag.class.getName().replace('.', '/')) && name.startsWith("get")) {
            System.out.println("Get: " + name);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitLabel(Label label) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitLdcInsn(Object value) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
        if (value instanceof String key) {
            System.out.println("Key: " + key);
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitEnd() {
        System.out.println(Thread.currentThread() .getStackTrace()[1].getMethodName());
    }
}

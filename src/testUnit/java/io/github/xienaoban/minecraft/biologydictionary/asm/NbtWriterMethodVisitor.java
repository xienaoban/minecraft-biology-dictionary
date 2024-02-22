package io.github.xienaoban.minecraft.biologydictionary.asm;

import io.github.xienaoban.minecraft.biologydictionary.platform.util.JavaNames;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;

import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;

public class NbtWriterMethodVisitor extends MethodVisitor {
    private final ClassWriter classWriter;
    private String nbtKey;
    private ArrayList<MethodBytecodeWriter> writers;

    public NbtWriterMethodVisitor(ClassWriter classWriter, MethodVisitor methodVisitor) {
        super(Opcodes.ASM9, methodVisitor);
        this.classWriter = classWriter;

        this.nbtKey = null;
        this.writers = new ArrayList<>();

        if (super.mv != null) {
            throw new RuntimeException("mv should be null");
        }
    }

    private void generateMethod() {
        if (nbtKey == null) {
            throw new RuntimeException("NBT key is null!");
        }
        String methodName = "write" + nbtKey;
        MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, methodName, NbtEntityClassVisitor.COMPOUND_TAG_SIGNATURE, null, null);

        methodVisitor.visitCode();
        for (MethodBytecodeWriter writer : writers) {
            writer.accept(methodVisitor);
        }
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
    }

    private void refreshMembers() {
        nbtKey = null;
        writers = new ArrayList<>();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        System.out.println("Method: " + Printer.OPCODES[opcode] + ", " + owner + ", " + name + ", " + descriptor + ", " + isInterface);
        writers.add(methodVisitor -> methodVisitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
        if (opcode == INVOKEVIRTUAL && owner.equals(NbtEntityClassVisitor.COMPOUND_TAG_NAME) && name.startsWith("put")) {
            System.out.println("Put: " + name);
            generateMethod();
            refreshMembers();
        } else if (opcode == INVOKESPECIAL && name.equals(JavaNames.ENTITY_WRITE_MORE_NBT) && descriptor.equals(NbtEntityClassVisitor.COMPOUND_TAG_SIGNATURE)) {
            refreshMembers();
        }
    }

    @Override
    public void visitLdcInsn(Object value) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        if (value instanceof String key) {
            nbtKey = key;
        }
        writers.add(methodVisitor -> methodVisitor.visitLdcInsn(value));
    }

    @Override
    public void visitParameter(String name, int access) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitParameter(name, access));
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitAnnotableParameterCount(parameterCount, visible));
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitAttribute(attribute));
    }

    @Override
    public void visitCode() {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitCode());
    }

    @Override
    public void visitInsn(int opcode) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitInsn(opcode));
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitIntInsn(opcode, operand));
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitVarInsn(opcode, varIndex));
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitTypeInsn(opcode, type));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitLabel(label));
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitIincInsn(varIndex, increment));
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitTableSwitchInsn(min, max, dflt, labels));
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitLookupSwitchInsn(dflt, keys, labels));
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitTryCatchBlock(start, end, handler, type));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
        writers.add(methodVisitor -> methodVisitor.visitLineNumber(line, start));
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitEnd() {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        throw new UnsupportedAsmVisit();
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
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        throw new UnsupportedAsmVisit();
    }
}

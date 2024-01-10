package io.github.xienaoban.minecraft.biologydictionary.asm;

public class UnsupportedAsmVisit extends RuntimeException {
    public UnsupportedAsmVisit() {
        super("\"" + Thread.currentThread().getStackTrace()[2].getMethodName() + "\" is not supported.");
    }
}

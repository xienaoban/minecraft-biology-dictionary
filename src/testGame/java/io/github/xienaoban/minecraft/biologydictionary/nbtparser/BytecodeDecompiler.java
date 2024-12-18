package io.github.xienaoban.minecraft.biologydictionary.nbtparser;

import com.strobel.assembler.metadata.CompilerTarget;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.stream.IntStream;

public final class BytecodeDecompiler {
    private static final Logger LOGGER = LogManager.getLogger();

    private enum Tool {
        Procyon, Fernflower, Cfr
    }

    private static final Tool TOOL = Tool.Procyon;

    /**
     * Decompile the class bytecode to java source code.
     * The returned source code is for Java Parser.
     */
    public static String decompile(Class<?> clazz) {
        // Procyon is better than Fernflower here according to my test.
        String source = switch (TOOL) {
            case Procyon -> decompileByProcyon(clazz);
            case Fernflower -> decompileByFernflower(clazz);
            default -> throw new AssertionError();
        };
        return preprocess(clazz, source);
    }

    public static String addLineNumber(String source) {
        String[] lines = source.split("\n");
        final int maxLineNumberLength = String.valueOf(lines.length).length();
        StringBuilder sb = new StringBuilder();
        IntStream.rangeClosed(1, lines.length).forEach(lineNumber -> sb.append(
                String.format("%" + maxLineNumberLength + "d: %s\n", lineNumber, lines[lineNumber - 1])
        ));
        return sb.toString();
    }

    private static String decompileByProcyon(Class<?> clazz) {
        PlainTextOutput output = new PlainTextOutput();
        java.util.logging.Logger.getLogger(com.strobel.assembler.metadata.signatures.Reifier.class.getSimpleName()).setLevel(java.util.logging.Level.OFF);
        DecompilerSettings settings = new DecompilerSettings();
        settings.setForcedCompilerTarget(CompilerTarget.JDK17);
        settings.setForceExplicitImports(true);
        com.strobel.decompiler.Decompiler.decompile(clazz.getName().replace('.', '/'), output, settings);
        return output.toString();
    }

    private static String decompileByFernflower(Class<?> clazz) {
        IBytecodeProvider bytecodeProvider = (externalPath, internalPath) -> getClassByteCode(clazz);
        IResultSaver resultSaver = new IResultSaver() {
            private String source = null;

            @Override public void saveFolder(String path) {}
            @Override public void copyFile(String source, String path, String entryName) {}
            @Override public void createArchive(String path, String archiveName, Manifest manifest) {}
            @Override public void saveDirEntry(String path, String archiveName, String entryName) {}
            @Override public void copyEntry(String source, String path, String archiveName, String entry) {}
            @Override public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {}
            @Override public void closeArchive(String path, String archiveName) {}

            @Override
            public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
                if (source != null) throw new AssertionError("The source has been set once?");
                source = content;
            }

            @Override
            public String toString() {
                if (source == null) throw new AssertionError("The source has not been set!");
                return source;
            }
        };

        IFernflowerLogger logger = new IFernflowerLogger() {
            @Override
            public void writeMessage(String message, Severity severity) {
                LOGGER.log(toLevel(severity), message);
            }

            @Override
            public void writeMessage(String message, Severity severity, Throwable t) {
                LOGGER.log(toLevel(severity), message);
            }

            private Level toLevel(Severity severity) {
                return switch (severity) {
                    case Severity.TRACE -> Level.TRACE;
                    case Severity.INFO -> Level.INFO;
                    case Severity.WARN -> Level.WARN;
                    case Severity.ERROR -> Level.ERROR;
                };
            }
        };
        Map<String, Object> options = new HashMap<>();
        options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
        options.put(IFernflowerPreferences.LAMBDA_TO_ANONYMOUS_CLASS, "1");
        options.put(IFernflowerPreferences.INDENT_STRING, "  ");
        options.put(IFernflowerPreferences.LOG_LEVEL, IFernflowerLogger.Severity.INFO.name());

        Fernflower fernflower = new Fernflower(bytecodeProvider, resultSaver, options, logger);
        fernflower.addSource(new File("ThisArgIsUselessHere", getClassPath(clazz))); // for older version
        // fernflower.getStructContext().addSpace(new File("ThisArgIsUselessHere", getClassPath(clazz)), true);
        fernflower.decompileContext();

        return resultSaver.toString();
    }

    private static byte[] getClassByteCode(Class<?> clazz) {
        String className = getClassPath(clazz);
        try (InputStream input = clazz.getClassLoader().getResourceAsStream(className)) {
            return Objects.requireNonNull(input).readAllBytes();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String getClassPath(Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    /**
     * Do some tricky processing on the source code to avoid Java Parser parsing failures.
     */
    private static String preprocess(Class<?> clazz, String source) {
        /*
        ```java
        final Predicate<Entity> no_CREATIVE_OR_SPECTATOR = EntitySelector.NO_CREATIVE_OR_SPECTATOR;
        Objects.requireNonNull(no_CREATIVE_OR_SPECTATOR);
        super(cat, class_, f, d, e, (Predicate<LivingEntity>)no_CREATIVE_OR_SPECTATOR::test);
        ```
        to
        ```java
        super(cat, class_, f, d, e, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
        ```
         */
        source = source.replaceAll(
                "final Predicate<Entity>.+\n +Objects.requireNonNull.+\n +super",
                "super"
        );
        source = source.replaceAll(
                ", class_, f, d, e, \\(Predicate<LivingEntity>\\).+::test\\);",
                ", class_, f, d, e, EntitySelector.PLACEHOLDER::test);"
        );

        /*
        ```java
        public enum ArmadilloState implements StringRepresentable permits Armadillo$ArmadilloState$1, Armadillo$ArmadilloState$2, Armadillo$ArmadilloState$3, Armadillo$ArmadilloState$4
        ```
        to
        ```java
        public enum ArmadilloState implements StringRepresentable
        ```
         */
        source = source.replaceAll(
                " permits [^{]+",
                " "
        );
        return source;
    }
}

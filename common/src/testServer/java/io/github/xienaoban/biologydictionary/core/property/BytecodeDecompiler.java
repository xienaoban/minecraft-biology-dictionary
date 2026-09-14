package io.github.xienaoban.biologydictionary.core.property;

import com.strobel.assembler.metadata.CompilerTarget;
import com.strobel.assembler.metadata.signatures.Reifier;
import com.strobel.decompiler.Decompiler;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class BytecodeDecompiler {
    private static final Logger LOGGER = LogManager.getLogger();

    private enum Tool {
        FabricSources, Procyon, Fernflower, Cfr
    }

    private static final Tool TOOL = Tool.Procyon;
    private static final String MINECRAFT_SOURCES_JAR_PROPERTY = "biologydictionary.minecraftSourcesJar";
    private static Path fabricSourcesJar;

    /**
     * Decompile the class bytecode to java source code.
     * The returned source code is for Java Parser.
     */
    public static String decompile(Class<?> clazz) {
        // Procyon is better than Fernflower here according to my test.
        String source = switch (TOOL) {
            case FabricSources -> getFabricSource(clazz);
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
        java.util.logging.Logger.getLogger(Reifier.class.getSimpleName())
            .setLevel(java.util.logging.Level.OFF);
        DecompilerSettings settings = new DecompilerSettings();
        settings.setForcedCompilerTarget(CompilerTarget.JDK17);
        settings.setForceExplicitImports(true);
        Decompiler.decompile(clazz.getName().replace('.', '/'), output, settings);
        return output.toString();
    }

    private static String getFabricSource(Class<?> clazz) {
        String sourcePath = clazz.getName().replace('.', '/') + ".java";
        return readSource(findFabricSourcesJar(sourcePath), sourcePath);
    }

    private static Path findFabricSourcesJar(String sourcePath) {
        if (fabricSourcesJar != null && containsSource(fabricSourcesJar, sourcePath)) {
            return fabricSourcesJar;
        }

        String explicitSourcesJar = System.getProperty(MINECRAFT_SOURCES_JAR_PROPERTY);
        if (explicitSourcesJar != null && !explicitSourcesJar.isBlank()) {
            Path sourcesJar = Path.of(explicitSourcesJar);
            if (containsSource(sourcesJar, sourcePath)) {
                fabricSourcesJar = sourcesJar;
                return sourcesJar;
            }
            throw new AssertionError(
                "Configured Minecraft sources jar does not contain " + sourcePath + ": " + sourcesJar
            );
        }

        Path loomCache = Path.of(".gradle", "loom-cache", "minecraftMaven", "net", "minecraft");
        if (!Files.exists(loomCache)) {
            throw new AssertionError("Fabric Loom Minecraft sources cache not found: " + loomCache);
        }
        try (Stream<Path> stream = Files.find(loomCache, 6, (path, attributes) -> attributes.isRegularFile()
                && path.getFileName().toString().endsWith("-sources.jar"))) {
            Path found = stream
                    .filter(path -> containsSource(path, sourcePath))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Minecraft source not found: " + sourcePath));
            fabricSourcesJar = found;
            return found;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static boolean containsSource(Path sourcesJar, String sourcePath) {
        try (JarFile jar = new JarFile(sourcesJar.toFile())) {
            return jar.getJarEntry(sourcePath) != null;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String readSource(Path sourcesJar, String sourcePath) {
        try (JarFile jar = new JarFile(sourcesJar.toFile())) {
            try (InputStream input = jar.getInputStream(Objects.requireNonNull(jar.getJarEntry(sourcePath)))) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
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
            public void saveClassFile(String path, String qualifiedName,
                                      String entryName, String content, int[] mapping) {
                if (source != null) { throw new AssertionError("The source has been set once?"); }
                source = content;
            }

            @Override
            public String toString() {
                if (source == null) { throw new AssertionError("The source has not been set!"); }
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
     * JavaParser now runs with JAVA_25 enabled, so no syntax workaround should be
     * needed here. Keep this hook only for narrowly-scoped, source-provider quirks.
     */
    private static String preprocess(Class<?> clazz, String source) {
        return source;
    }
}

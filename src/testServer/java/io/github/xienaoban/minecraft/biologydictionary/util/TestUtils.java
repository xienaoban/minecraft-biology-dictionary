package io.github.xienaoban.minecraft.biologydictionary.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {
    public static final Path GAME_ROOT;
    public static final Path PROJECT_ROOT;
    public static final Path SRC_ROOT;
    public static final Path MAIN_ROOT;
    public static final Path MAIN_JAVA_ROOT;
    public static final Path TEST_ROOT;
    public static final Path TEST_JAVA_ROOT;

    static {
        GAME_ROOT = Paths.get("").toAbsolutePath();
        if (!GAME_ROOT.endsWith(Paths.get("build", "testServer"))) {
            throw new AssertionError("GAME_ROOT=" + GAME_ROOT);
        }
        PROJECT_ROOT = GAME_ROOT.getParent().getParent();
        SRC_ROOT = Paths.get(PROJECT_ROOT.toString(), "src");
        if (!Files.isDirectory(SRC_ROOT)) {
            throw new AssertionError("SRC_ROOT=" + SRC_ROOT);
        }

        MAIN_ROOT = Paths.get(SRC_ROOT.toString(), "main");
        if (!Files.isDirectory(MAIN_ROOT)) {
            throw new AssertionError("MAIN_ROOT=" + MAIN_ROOT);
        }
        MAIN_JAVA_ROOT = Paths.get(MAIN_ROOT.toString(), "java");
        if (!Files.isDirectory(MAIN_JAVA_ROOT)) {
            throw new AssertionError("MAIN_JAVA_ROOT=" + MAIN_JAVA_ROOT);
        }

        TEST_ROOT = Paths.get(SRC_ROOT.toString(), "testServer");
        if (!Files.isDirectory(TEST_ROOT)) {
            throw new AssertionError("TEST_ROOT=" + TEST_ROOT);
        }
        TEST_JAVA_ROOT = Paths.get(TEST_ROOT.toString(), "java");
        if (!Files.isDirectory(TEST_JAVA_ROOT)) {
            throw new AssertionError("TEST_JAVA_ROOT=" + TEST_JAVA_ROOT);
        }
    }
}

package io.github.xienaoban.biologydictionary.util;

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

    public static final String ENTITY_READ_NBT = "load";
    public static final String ENTITY_WRITE_NBT = "saveWithoutId";
    public static final String ENTITY_READ_ADDITIONAL_NBT = "readAdditionalSaveData";
    public static final String ENTITY_WRITE_ADDITIONAL_NBT = "addAdditionalSaveData";

    static {
        GAME_ROOT = Paths.get("").toAbsolutePath();
        if (!GAME_ROOT.endsWith(Paths.get("build", "testServer"))) {
            throw new AssertionError("GAME_ROOT=" + GAME_ROOT);
        }
        PROJECT_ROOT = GAME_ROOT.getParent().getParent().getParent();
        Path readme = Paths.get(PROJECT_ROOT.toString(), "README.md");
        if (!Files.isRegularFile(readme)) {
            throw new AssertionError("PROJECT_ROOT=" + PROJECT_ROOT + " does not contain README.md");
        }
        SRC_ROOT = Paths.get(PROJECT_ROOT.toString(), "common", "src");
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

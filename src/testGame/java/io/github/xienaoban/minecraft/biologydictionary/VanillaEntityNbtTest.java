package io.github.xienaoban.minecraft.biologydictionary;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.PlainTextOutput;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
            if (cur.getClazz() != Animal.class) return true;
            decompile(cur.getClazz());
            return true;
        });
        helper.succeed();
    }

    private static void decompile(Class<? extends Entity> clazz) {
        PlainTextOutput output = new PlainTextOutput();
        Decompiler.decompile(clazz.getName().replace('.', '/'), output);
        String source = output.toString();

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        JavaParser parser = new JavaParser(config);
        ParseResult<CompilationUnit> parseResult = parser.parse(source);
        if (!parseResult.isSuccessful()) throw new RuntimeException(parseResult.getProblems().toString());
        Optional<CompilationUnit> result = parseResult.getResult();
        if (result.isEmpty()) throw new RuntimeException("Empty CompilationUnit?!");
        CompilationUnit unit = result.get();
        LOGGER.info("~~~" + unit.toString());
    }
}

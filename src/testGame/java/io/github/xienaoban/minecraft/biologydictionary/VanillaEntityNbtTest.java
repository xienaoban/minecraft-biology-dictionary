package io.github.xienaoban.minecraft.biologydictionary;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.PlainTextOutput;
import io.github.xienaoban.minecraft.biologydictionary.core.EntityManager;
import io.github.xienaoban.minecraft.biologydictionary.javaparser.ReadAdditionalNbtVisitor;
import io.github.xienaoban.minecraft.biologydictionary.platform.util.JavaNames;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.logging.Level;

public class VanillaEntityNbtTest implements FabricGameTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @GameTest(template = EMPTY_STRUCTURE)
    public void testNbtElements(GameTestHelper helper) {
        Pair<CompilationUnit, ClassOrInterfaceDeclaration> res = createNbtGetterAndSetterClass();
        CompilationUnit cu = res.a;
        ClassOrInterfaceDeclaration cl = res.b;
        try {
            EntityManager.getInstance().dfsEntityTree(true, (cur, depth) -> {
//                if (cur.getClazz() != LivingEntity.class) return true;
                parse(cur.getClazz(), cl);
                return true;
            });
            LOGGER.info("Class:\n" + cu);
            helper.succeed();
        } catch (AssertionError e) {
            LOGGER.error("Current class:\n" + cu);
            throw e;
        }
    }

    private static Pair<CompilationUnit, ClassOrInterfaceDeclaration> createNbtGetterAndSetterClass() {
        CompilationUnit res = new CompilationUnit("io.github.xienaoban.minecraft.biologydictionary");
        ClassOrInterfaceDeclaration clazz = res.addClass("AutoGenEntityNbtApi", Modifier.Keyword.PUBLIC, Modifier.Keyword.FINAL);
        return new Pair<>(res, clazz);
    }

    private static void parse(Class<? extends Entity> entityClazz, ClassOrInterfaceDeclaration res) {
        LOGGER.info("Start parsing entity class: " + entityClazz);
        PlainTextOutput output = new PlainTextOutput();
        java.util.logging.Logger.getLogger(com.strobel.assembler.metadata.signatures.Reifier.class.getSimpleName()).setLevel(Level.OFF);
        Decompiler.decompile(entityClazz.getName().replace('.', '/'), output);
        String source = output.toString();

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        JavaParser parser = new JavaParser(config);
        ParseResult<CompilationUnit> parseResult = parser.parse(source);
        if (!parseResult.isSuccessful()) throw new AssertionError(parseResult.getProblems().toString());
        Optional<CompilationUnit> result = parseResult.getResult();
        if (result.isEmpty()) throw new AssertionError("Empty CompilationUnit?!");
        CompilationUnit cu = result.get();
        VoidVisitor<ClassOrInterfaceDeclaration> methodVisitor = new AdditionalNbtMethodsVisitor(entityClazz);
        methodVisitor.visit(cu, res);
    }

    private static final class AdditionalNbtMethodsVisitor extends VoidVisitorAdapter<ClassOrInterfaceDeclaration> {
        private final Class<? extends Entity> entityClazz;

        private AdditionalNbtMethodsVisitor(Class<? extends Entity> entityClazz) {
            this.entityClazz = entityClazz;
        }

        @Override
        public void visit(MethodDeclaration md, ClassOrInterfaceDeclaration res) {
            super.visit(md, res);
            final VoidVisitor<Void> visitor;
            if (JavaNames.ENTITY_READ_ADDITIONAL_NBT.equals(md.getNameAsString())) {
                visitor = new ReadAdditionalNbtVisitor(res, entityClazz);
            } else if (JavaNames.ENTITY_WRITE_ADDITIONAL_NBT.equals(md.getNameAsString())) {
                return;
            } else return;

            BlockStmt blockStmt = md.getBody().orElseThrow(() -> new AssertionError("No body?!"));
            // new PrintNodeVisitor<>().visit(blockStmt, res);
            visitor.visit(blockStmt, null);
        }
    }
}

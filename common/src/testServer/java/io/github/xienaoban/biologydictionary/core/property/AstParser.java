package io.github.xienaoban.biologydictionary.core.property;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.util.Objects;
import java.util.Optional;

public final class AstParser {

    public static CompilationUnit generateAst(Class<?> clazz) {
        String source = BytecodeDecompiler.decompile(clazz);
        return generateAst(source);
    }

    public static CompilationUnit generateAst(String source) {
        Objects.requireNonNull(source);

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_25);

        JavaParser parser = new JavaParser(config);
        ParseResult<CompilationUnit> parseResult = parser.parse(source);
        if (!parseResult.isSuccessful()) { throw new AssertionError(parseResult.getProblems().toString()); }

        Optional<CompilationUnit> result = parseResult.getResult();
        if (result.isEmpty()) { throw new AssertionError("Empty CompilationUnit?!"); }
        return result.get();
    }

    public static void addImport(Node node, String clazzNameToImport) {
        Node cur = node;
        while (!(cur instanceof CompilationUnit cu)) {
            cur = node.getParentNode().orElseThrow(() -> new AssertionError("No parent of CompilationUnit?!"));
        }
        cu.addImport(clazzNameToImport);
    }
}

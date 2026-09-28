package com.codelens.ai.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CodeValidator {

    private final JavaParser javaParser;
    
    private static final List<String> PYTHON_KEYWORDS = Arrays.asList(
            "def ", "class ", "import ", "from ", "if ", "else:", "elif ", "for ", "while ",
            "return", "print(", "try:", "except", "with "
    );

    public CodeValidator() {
        this.javaParser = new JavaParser();
    }

    public boolean isValidCode(String language, String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        String cleanCode = code.trim();

        if ("java".equalsIgnoreCase(language)) {
            return isValidJava(cleanCode);
        } else if ("python".equalsIgnoreCase(language)) {
            return isValidPython(cleanCode);
        }

        // Unknown language, assume valid or handle differently
        return true;
    }

    private boolean isValidJava(String code) {
        ParseResult<CompilationUnit> result = javaParser.parse(code);
        // If it parses successfully or has minor errors but looks like Java, we accept.
        // If it's complete gibberish, it won't parse into a meaningful AST structure.
        if (result.isSuccessful()) {
            return true;
        }
        
        // If it's a snippet (e.g. just a method), JavaParser might fail. 
        // We'll do a fallback heuristic check:
        return hasJavaStructure(code);
    }
    
    private boolean hasJavaStructure(String code) {
        // Fallback for Java snippets: must contain basic structure characters
        if (!code.contains(";") && !code.contains("{") && !code.contains("}")) {
            return false; // Random text without semicolons or braces isn't Java
        }
        
        // Check for common keywords
        boolean hasKeyword = code.contains("public") || code.contains("private") || 
                             code.contains("class") || code.contains("interface") || 
                             code.contains("void") || code.contains("String") ||
                             code.contains("int ") || code.contains("return");
                             
        // Check if it's just a single long word (gibberish)
        if (!code.contains(" ") && code.length() > 20) {
            return false;
        }
                             
        return hasKeyword;
    }

    private boolean isValidPython(String code) {
        // Simple heuristic for Python
        // Reject plain random text like "fgfffffffffffff"
        
        // If it doesn't contain a space and is longer than 20 chars, it's likely gibberish
        if (!code.contains(" ") && !code.contains("\n") && code.length() > 20) {
            return false;
        }
        
        // For very short strings that aren't keywords
        if (!code.contains(" ") && !code.contains("\n") && code.length() < 20) {
            // Might be a single variable name, but for a code review it's likely invalid context
            return false;
        }
        
        // Look for typical Python structures
        boolean hasStructure = code.contains("=") || code.contains(":") || code.contains("(");
        
        boolean hasKeyword = PYTHON_KEYWORDS.stream().anyMatch(code::contains);
        
        return hasStructure || hasKeyword;
    }
}

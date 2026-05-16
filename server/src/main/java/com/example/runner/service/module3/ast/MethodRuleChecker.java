package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.MethodRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.List;
import java.util.Optional;

/**
 * Проверяет объявление метода в исходном коде
 */
public class MethodRuleChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки метода
     * @return результат проверки
     */
    public AstValidationResult check(String code, MethodRule rule) {
        AstValidationResult result = new AstValidationResult();

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(code);
        } catch (Exception e) {
            result.addError("Ошибка парсинга кода: " + e.getMessage());
            return result;
        }

        String simpleName = simpleName(rule.getClassName());

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.findFirst(
                ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(simpleName)
        );

        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration cls = classOpt.get();

        List<MethodDeclaration> candidates = cls.getMethodsByName(rule.getMethodName());

        Optional<MethodDeclaration> methodOpt;
        if (rule.getParameters() != null && !rule.getParameters().isEmpty()) {
            methodOpt = candidates.stream()
                    .filter(m -> paramsMatch(m, rule.getParameters()))
                    .findFirst();
        } else {
            methodOpt = candidates.stream().findFirst();
        }

        if (methodOpt.isEmpty()) {
            if (rule.isRequired()) {
                result.addError(rule.getErrorMessage() != null
                        ? rule.getErrorMessage()
                        : "Метод " + rule.getMethodName()
                                + " не найден в классе " + simpleName);
            }
            return result;
        }

        MethodDeclaration method = methodOpt.get();

        if (rule.getReturnType() != null && !rule.getReturnType().isBlank()) {
            String expected = simpleName(rule.getReturnType());
            String actual   = simpleName(method.getTypeAsString());
            if (!actual.equals(expected)) {
                result.addError("Метод " + rule.getMethodName()
                        + " должен возвращать " + expected
                        + " (найдено: " + actual + ")");
            }
        }

        if (rule.getModifiers() != null) {
            for (String mod : rule.getModifiers()) {
                if (!hasModifier(method, mod)) {
                    result.addError("Метод " + rule.getMethodName()
                            + " должен иметь модификатор " + mod);
                }
            }
        }

        if (rule.getAnnotations() != null) {
            for (String annotation : rule.getAnnotations()) {
                String simpleAnnotation = annotation.startsWith("@")
                        ? annotation.substring(1) : annotation;
                boolean found = method.getAnnotations().stream()
                        .map(AnnotationExpr::getNameAsString)
                        .anyMatch(n -> n.equals(simpleAnnotation));
                if (!found) {
                    result.addError("Метод " + rule.getMethodName()
                            + " должен иметь аннотацию @" + simpleAnnotation);
                }
            }
        }

        return result;
    }

    private boolean paramsMatch(MethodDeclaration method, List<String> expectedParams) {
        var actualParams = method.getParameters();
        if (actualParams.size() != expectedParams.size()) return false;

        for (int i = 0; i < actualParams.size(); i++) {
            String actual   = simpleName(actualParams.get(i).getTypeAsString());
            String expected = simpleName(expectedParams.get(i));
            if (!actual.equals(expected)) return false;
        }
        return true;
    }

    private boolean hasModifier(MethodDeclaration method, String modifier) {
        return switch (modifier.toLowerCase()) {
            case "public"    -> method.hasModifier(Modifier.Keyword.PUBLIC);
            case "protected" -> method.hasModifier(Modifier.Keyword.PROTECTED);
            case "private"   -> method.hasModifier(Modifier.Keyword.PRIVATE);
            case "static"    -> method.hasModifier(Modifier.Keyword.STATIC);
            case "final"     -> method.hasModifier(Modifier.Keyword.FINAL);
            case "abstract"  -> method.hasModifier(Modifier.Keyword.ABSTRACT);
            default          -> false;
        };
    }

    private String simpleName(String name) {
        if (name == null) return "";
        String stripped = name.replaceAll("<.*>", "").trim();
        int dot = stripped.lastIndexOf('.');
        return dot >= 0 ? stripped.substring(dot + 1) : stripped;
    }
}

package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.AnnotationRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Проверяет наличие и отсутствие аннотаций на классах и методах
 */
public class AnnotationChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки аннотаций
     * @return результат проверки
     */
    public AstValidationResult check(String code, AnnotationRule rule) {
        AstValidationResult result = new AstValidationResult();

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(code);
        } catch (Exception e) {
            result.addError("Ошибка парсинга кода: " + e.getMessage());
            return result;
        }

        if ("class".equals(rule.getTargetType())) {
            checkClassAnnotations(cu, rule, result);
        } else if ("method".equals(rule.getTargetType())) {
            checkMethodAnnotations(cu, rule, result);
        } else {
            result.addWarning("Неизвестный targetType: " + rule.getTargetType()
                    + ". Ожидается 'class' или 'method'.");
        }

        return result;
    }

    private void checkClassAnnotations(CompilationUnit cu, AnnotationRule rule,
                                        AstValidationResult result) {
        String simpleName = simpleName(rule.getClassName());
        Optional<ClassOrInterfaceDeclaration> classOpt = cu.findFirst(
                ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(simpleName));

        if (classOpt.isEmpty()) {
            return;
        }

        List<String> present = classOpt.get().getAnnotations().stream()
                .map(AnnotationExpr::getNameAsString)
                .collect(Collectors.toList());

        checkAnnotations(present, rule, "Класс " + simpleName, result);
    }

    private void checkMethodAnnotations(CompilationUnit cu, AnnotationRule rule,
                                         AstValidationResult result) {
        List<MethodDeclaration> candidates;
        if (rule.getClassName() != null && !rule.getClassName().isBlank()) {
            String simpleName = simpleName(rule.getClassName());
            Optional<ClassOrInterfaceDeclaration> classOpt = cu.findFirst(
                    ClassOrInterfaceDeclaration.class,
                    c -> c.getNameAsString().equals(simpleName));
            if (classOpt.isEmpty()) {
                return;
            }
            candidates = classOpt.get().getMethodsByName(rule.getMethodName());
        } else {
            candidates = cu.findAll(MethodDeclaration.class,
                    m -> m.getNameAsString().equals(rule.getMethodName()));
        }

        if (candidates.isEmpty()) {
            result.addError("Метод " + rule.getMethodName() + " не найден");
            return;
        }

        MethodDeclaration method = candidates.get(0);
        List<String> present = method.getAnnotations().stream()
                .map(AnnotationExpr::getNameAsString)
                .collect(Collectors.toList());

        checkAnnotations(present, rule, "Метод " + rule.getMethodName(), result);
    }

    private void checkAnnotations(List<String> present, AnnotationRule rule,
                                   String target, AstValidationResult result) {
        if (rule.getRequiredAnnotations() != null) {
            for (String required : rule.getRequiredAnnotations()) {
                String clean = required.startsWith("@") ? required.substring(1) : required;
                if (!present.contains(clean)) {
                    result.addError(rule.getErrorMessage() != null
                            ? rule.getErrorMessage()
                            : target + " должен иметь аннотацию @" + clean);
                }
            }
        }

        if (rule.getForbiddenAnnotations() != null) {
            for (String forbidden : rule.getForbiddenAnnotations()) {
                String clean = forbidden.startsWith("@") ? forbidden.substring(1) : forbidden;
                if (present.contains(clean)) {
                    result.addError(target + " не должен иметь аннотацию @" + clean);
                }
            }
        }
    }

    private String simpleName(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }
}

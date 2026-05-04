package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.FieldRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.Optional;

public class FieldRuleChecker {

    public AstValidationResult check(String code, FieldRule rule) {
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

        Optional<FieldDeclaration> fieldOpt = cls.getFields().stream()
                .filter(f -> f.getVariables().stream()
                        .anyMatch(v -> v.getNameAsString().equals(rule.getFieldName())))
                .findFirst();

        if (fieldOpt.isEmpty()) {
            if (rule.isRequired()) {
                result.addError(rule.getErrorMessage() != null
                        ? rule.getErrorMessage()
                        : "Поле " + rule.getFieldName()
                                + " не найдено в классе " + simpleName);
            }
            return result;
        }

        FieldDeclaration field = fieldOpt.get();

        if (rule.getFieldType() != null && !rule.getFieldType().isBlank()) {
            String expectedType = simpleName(rule.getFieldType());
            String actualType   = field.getVariables().stream()
                    .filter(v -> v.getNameAsString().equals(rule.getFieldName()))
                    .findFirst()
                    .map(VariableDeclarator::getTypeAsString)
                    .orElse("");
            String actualSimple = simpleName(actualType);
            if (!actualSimple.equals(expectedType)) {
                result.addError("Поле " + rule.getFieldName()
                        + " должно иметь тип " + expectedType
                        + " (найдено: " + actualSimple + ")");
            }
        }

        if (rule.getModifiers() != null) {
            for (String mod : rule.getModifiers()) {
                if (!hasModifier(field, mod)) {
                    result.addError("Поле " + rule.getFieldName()
                            + " должно иметь модификатор " + mod);
                }
            }
        }

        return result;
    }

    private boolean hasModifier(FieldDeclaration field, String modifier) {
        return switch (modifier.toLowerCase()) {
            case "public"    -> field.hasModifier(Modifier.Keyword.PUBLIC);
            case "protected" -> field.hasModifier(Modifier.Keyword.PROTECTED);
            case "private"   -> field.hasModifier(Modifier.Keyword.PRIVATE);
            case "static"    -> field.hasModifier(Modifier.Keyword.STATIC);
            case "final"     -> field.hasModifier(Modifier.Keyword.FINAL);
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
package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.ConstructorRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Проверяет объявление и тело конструктора
 */
public class ConstructorRuleChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки конструктора
     * @return результат проверки
     */
    public AstValidationResult check(String code, ConstructorRule rule) {
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
        List<ConstructorDeclaration> constructors = cls.getConstructors();

        if (constructors.isEmpty()) {
            if (rule.isRequired()) {
                result.addError(rule.getErrorMessage() != null
                        ? rule.getErrorMessage()
                        : "Конструктор не найден в классе " + simpleName);
            }
            return result;
        }

        Optional<ConstructorDeclaration> ctorOpt;
        if (rule.getParameters() != null && !rule.getParameters().isEmpty()) {
            ctorOpt = constructors.stream()
                    .filter(c -> paramsMatch(c, rule.getParameters()))
                    .findFirst();

            if (ctorOpt.isEmpty()) {
                result.addError(rule.getErrorMessage() != null
                        ? rule.getErrorMessage()
                        : "Конструктор с параметрами " + rule.getParameters()
                                + " не найден в классе " + simpleName);
                return result;
            }
        } else {
            ctorOpt = constructors.stream().findFirst();
        }

        ConstructorDeclaration ctor = ctorOpt.get();

        if (rule.getModifiers() != null) {
            for (String mod : rule.getModifiers()) {
                if (!hasModifier(ctor, mod)) {
                    result.addError("Конструктор класса " + simpleName
                            + " должен иметь модификатор " + mod);
                }
            }
        }

        checkConstructorBody(ctor, rule, simpleName, result);

        return result;
    }

    private void checkConstructorBody(ConstructorDeclaration ctor, ConstructorRule rule,
                                      String className, AstValidationResult result) {

        List<String> calls = new ArrayList<>();
        ctor.findAll(MethodCallExpr.class).forEach(call -> {
            String name = call.getNameAsString();
            if (call.getScope().isPresent()) {
                String scope = normalizeScope(call.getScope().get().toString());
                calls.add(scope + "." + name);
            }
            calls.add(name);
        });

        if (rule.getRequiredMethodCalls() != null) {
            for (String required : rule.getRequiredMethodCalls()) {
                if (!callPresent(calls, required)) {
                    result.addError(rule.getErrorMessage() != null
                            ? rule.getErrorMessage()
                            : "Конструктор класса " + className
                                    + " должен вызывать: " + required);
                }
            }
        }

        if (rule.getForbiddenMethodCalls() != null) {
            for (String forbidden : rule.getForbiddenMethodCalls()) {
                if (callPresent(calls, forbidden)) {
                    result.addError("Конструктор класса " + className
                            + " не должен вызывать: " + forbidden);
                }
            }
        }

        if (rule.getRequiredStatements() != null) {
            String ctorBody = ctor.getBody().toString();
            for (String stmt : rule.getRequiredStatements()) {
                if (!ctorBody.contains(stmt)) {
                    result.addError("Конструктор класса " + className
                            + " должен содержать: " + stmt);
                }
            }
        }
    }

    private String normalizeScope(String scope) {
        if (scope.startsWith("this.")) {
            return scope.substring(5);
        }
        return scope;
    }

    private boolean callPresent(List<String> calls, String required) {
        if (required.endsWith(".*")) {
            String prefix = required.substring(0, required.length() - 2) + ".";
            return calls.stream().anyMatch(c -> c.startsWith(prefix));
        }
        return calls.stream().anyMatch(c ->
                c.equals(required) || c.endsWith("." + required));
    }

    private boolean paramsMatch(ConstructorDeclaration ctor, List<String> expectedParams) {
        var actualParams = ctor.getParameters();
        if (actualParams.size() != expectedParams.size()) return false;

        for (int i = 0; i < actualParams.size(); i++) {
            String actual   = simpleName(actualParams.get(i).getTypeAsString());
            String expected = simpleName(expectedParams.get(i));
            if (!actual.equals(expected)) return false;
        }
        return true;
    }

    private boolean hasModifier(ConstructorDeclaration ctor, String modifier) {
        return switch (modifier.toLowerCase()) {
            case "public"    -> ctor.hasModifier(Modifier.Keyword.PUBLIC);
            case "protected" -> ctor.hasModifier(Modifier.Keyword.PROTECTED);
            case "private"   -> ctor.hasModifier(Modifier.Keyword.PRIVATE);
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

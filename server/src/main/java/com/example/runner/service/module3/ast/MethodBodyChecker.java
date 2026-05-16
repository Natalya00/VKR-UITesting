package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.MethodBodyRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SuperExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Проверяет тело метода: вызовы, return this, super и обязательные операторы
 */
public class MethodBodyChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки тела метода
     * @return результат проверки
     */
    public AstValidationResult check(String code, MethodBodyRule rule) {
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
                c -> c.getNameAsString().equals(simpleName));

        if (classOpt.isEmpty()) {
            return result;
        }

        Optional<MethodDeclaration> methodOpt = classOpt.get()
                .getMethods().stream()
                .filter(m -> m.getNameAsString().equals(rule.getMethodName()))
                .findFirst();

        if (methodOpt.isEmpty()) {
            result.addError("Метод " + rule.getMethodName()
                    + " не найден в классе " + simpleName);
            return result;
        }

        MethodDeclaration method = methodOpt.get();
        List<String> calls = extractMethodCalls(method);

        if (rule.getRequiredMethodCalls() != null) {
            for (String required : rule.getRequiredMethodCalls()) {
                if (required.contains("|")) {
                    String[] alternatives = required.split("\\|");
                    boolean anyFound = false;
                    for (String alt : alternatives) {
                        if (callPresent(calls, alt.trim())) {
                            anyFound = true;
                            break;
                        }
                    }
                    if (!anyFound) {
                        result.addError(rule.getErrorMessage() != null
                                ? rule.getErrorMessage()
                                : "Метод " + rule.getMethodName()
                                        + "() должен вызывать один из: " + String.join(", ", alternatives));
                    }
                } else {
                    if (!callPresent(calls, required)) {
                        result.addError(rule.getErrorMessage() != null
                                ? rule.getErrorMessage()
                                : "Метод " + rule.getMethodName()
                                        + "() должен вызывать: " + required);
                    }
                }
            }
        }

        if (rule.getForbiddenMethodCalls() != null) {
            for (String forbidden : rule.getForbiddenMethodCalls()) {
                if (callPresent(calls, forbidden)) {
                    result.addError("Метод " + rule.getMethodName()
                            + "() не должен вызывать: " + forbidden);
                }
            }
        }

        if (Boolean.TRUE.equals(rule.getMustReturnThis())) {
            boolean hasReturnThis = method.findAll(ReturnStmt.class).stream()
                    .anyMatch(r -> r.getExpression()
                            .filter(e -> e instanceof ThisExpr).isPresent());
            if (!hasReturnThis) {
                result.addError("Метод " + rule.getMethodName()
                        + "() должен возвращать this для поддержки Fluent API");
            }
        }

        if (rule.getMustCallSuper() != null && !rule.getMustCallSuper().isBlank()) {
            String superMethod = rule.getMustCallSuper();
            boolean callsSuper = method.findAll(MethodCallExpr.class).stream()
                    .anyMatch(c -> c.getNameAsString().equals(superMethod)
                            && c.getScope().filter(s -> s instanceof SuperExpr).isPresent());
            if (!callsSuper) {
                result.addError("Метод " + rule.getMethodName()
                        + "() должен вызывать super." + superMethod + "()");
            }
        }

        if (rule.getRequiredStatements() != null) {
            String methodBody = method.getBody()
                    .map(Object::toString).orElse("");
            for (String stmt : rule.getRequiredStatements()) {
                if (!methodBody.contains(stmt)) {
                    result.addError("Метод " + rule.getMethodName()
                            + "() должен содержать: " + stmt);
                }
            }
        }

        return result;
    }

    private List<String> extractMethodCalls(MethodDeclaration method) {
        List<String> calls = new ArrayList<>();
        method.findAll(MethodCallExpr.class).forEach(call -> {
            String name = call.getNameAsString();
            if (call.getScope().isPresent()) {
                String scope = normalizeScope(call.getScope().get().toString());
                calls.add(scope + "." + name);
            }
            calls.add(name);
        });
        return calls;
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

    private String simpleName(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }
}

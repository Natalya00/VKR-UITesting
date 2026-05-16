package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.InterfaceRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.Optional;

/**
 * Проверяет объявление интерфейса и его методов
 */
public class InterfaceRuleChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки интерфейса
     * @return результат проверки
     */
    public AstValidationResult check(String code, InterfaceRule rule) {
        AstValidationResult result = new AstValidationResult();

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(code);
        } catch (Exception e) {
            result.addError("Ошибка парсинга кода: " + e.getMessage());
            return result;
        }

        String simpleName = simpleName(rule.getInterfaceName());

        Optional<ClassOrInterfaceDeclaration> ifaceOpt = cu.findFirst(
                ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(simpleName) && c.isInterface()
        );

        if (ifaceOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration iface = ifaceOpt.get();

        if (rule.getRequiredMethods() != null) {
            for (String methodName : rule.getRequiredMethods()) {
                boolean found = iface.getMethods().stream()
                        .anyMatch(m -> m.getNameAsString().equals(methodName));
                if (!found) {
                    result.addError("Интерфейс " + simpleName
                            + " должен объявлять метод " + methodName + "()");
                }
            }
        }

        return result;
    }

    private String simpleName(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }
}

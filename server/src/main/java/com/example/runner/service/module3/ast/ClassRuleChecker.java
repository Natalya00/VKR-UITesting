package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.ClassRule;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.Optional;

/**
 * Проверяет объявление класса в исходном коде по {@link ClassRule}
 * Использует JavaParser для анализа наследования, интерфейсов и модификаторов.
 */
public class ClassRuleChecker {

    /**
     * @param code исходный Java-код
     * @param rule правило проверки класса
     * @return результат проверки
     */
    public AstValidationResult check(String code, ClassRule rule) {
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
                c -> c.getNameAsString().equals(simpleName) && !c.isInterface()
        );

        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration cls = classOpt.get();

        if (rule.getExtendsClass() != null && !rule.getExtendsClass().isBlank()) {
            String expectedParent = simpleName(rule.getExtendsClass());
            boolean extendsOk = cls.getExtendedTypes().stream()
                    .anyMatch(t -> t.getNameAsString().equals(expectedParent));
            if (!extendsOk) {
                result.addError(rule.getErrorMessage() != null
                        ? rule.getErrorMessage()
                        : "Класс " + simpleName + " должен наследоваться от " + expectedParent);
            }
        }

        if (rule.getImplementsInterfaces() != null) {
            for (String iface : rule.getImplementsInterfaces()) {
                String simpleIface = simpleName(iface);
                boolean implOk = cls.getImplementedTypes().stream()
                        .anyMatch(t -> t.getNameAsString().equals(simpleIface));
                if (!implOk) {
                    result.addError(rule.getErrorMessage() != null
                            ? rule.getErrorMessage()
                            : "Класс " + simpleName + " должен реализовывать интерфейс " + simpleIface);
                }
            }
        }

        if (rule.getModifiers() != null) {
            for (String mod : rule.getModifiers()) {
                if (!hasModifier(cls, mod)) {
                    result.addError(rule.getErrorMessage() != null
                            ? rule.getErrorMessage()
                            : "Класс " + simpleName + " должен иметь модификатор " + mod);
                }
            }
        }

        return result;
    }

    private boolean hasModifier(ClassOrInterfaceDeclaration cls, String modifier) {
        return switch (modifier.toLowerCase()) {
            case "public"    -> cls.hasModifier(Modifier.Keyword.PUBLIC);
            case "protected" -> cls.hasModifier(Modifier.Keyword.PROTECTED);
            case "private"   -> cls.hasModifier(Modifier.Keyword.PRIVATE);
            case "abstract"  -> cls.isAbstract();
            case "final"     -> cls.isFinal();
            default          -> false;
        };
    }

    private String simpleName(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }
}

package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.AnnotationRule;
import com.example.runner.dto.module3.AstRules;
import com.example.runner.dto.module3.ClassRule;
import com.example.runner.dto.module3.ConstructorRule;
import com.example.runner.dto.module3.FieldRule;
import com.example.runner.dto.module3.InterfaceRule;
import com.example.runner.dto.module3.MethodBodyRule;
import com.example.runner.dto.module3.MethodRule;

/**
 * Координатор AST-валидации исходного Java-кода
 * Делегирует проверку специализированным checker-классам
 */
public class AstValidator {

    private final ClassRuleChecker       classRuleChecker;
    private final MethodRuleChecker      methodRuleChecker;
    private final FieldRuleChecker       fieldRuleChecker;
    private final ConstructorRuleChecker constructorRuleChecker;
    private final InterfaceRuleChecker   interfaceRuleChecker;
    private final MethodBodyChecker      methodBodyChecker;
    private final AnnotationChecker      annotationChecker;

    /** Создаёт валидатор со всеми checker-компонентами */
    public AstValidator() {
        this.classRuleChecker       = new ClassRuleChecker();
        this.methodRuleChecker      = new MethodRuleChecker();
        this.fieldRuleChecker       = new FieldRuleChecker();
        this.constructorRuleChecker = new ConstructorRuleChecker();
        this.interfaceRuleChecker   = new InterfaceRuleChecker();
        this.methodBodyChecker      = new MethodBodyChecker();
        this.annotationChecker      = new AnnotationChecker();
    }

    /**
     * Проверяет код по всем правилам из {@link AstRules}
     * @param code  исходный Java-код
     * @param rules набор AST-правил; при null возвращается успешный результат
     * @return результат проверки
     */
    public AstValidationResult validate(String code, AstRules rules) {
        AstValidationResult result = new AstValidationResult();

        if (code == null || code.isBlank()) {
            result.addError("Код пуст");
            return result;
        }

        if (rules == null) return result;

        if (rules.getClassRules() != null) {
            for (ClassRule r : rules.getClassRules()) {
                result.merge(classRuleChecker.check(code, r));
            }
        }

        if (rules.getMethodRules() != null) {
            for (MethodRule r : rules.getMethodRules()) {
                result.merge(methodRuleChecker.check(code, r));
            }
        }

        if (rules.getFieldRules() != null) {
            for (FieldRule r : rules.getFieldRules()) {
                result.merge(fieldRuleChecker.check(code, r));
            }
        }

        if (rules.getConstructorRules() != null) {
            for (ConstructorRule r : rules.getConstructorRules()) {
                result.merge(constructorRuleChecker.check(code, r));
            }
        }

        if (rules.getInterfaceRules() != null) {
            for (InterfaceRule r : rules.getInterfaceRules()) {
                result.merge(interfaceRuleChecker.check(code, r));
            }
        }

        if (rules.getMethodBodyRules() != null) {
            for (MethodBodyRule r : rules.getMethodBodyRules()) {
                result.merge(methodBodyChecker.check(code, r));
            }
        }

        if (rules.getAnnotationRules() != null) {
            for (AnnotationRule r : rules.getAnnotationRules()) {
                result.merge(annotationChecker.check(code, r));
            }
        }

        return result;
    }

    /**
     * Проверяет код только по правилам указанного класса
     * @param code      исходный Java-код
     * @param className имя класса для фильтрации правил
     * @param rules     набор AST-правил
     * @return результат проверки правил класса
     */
    public AstValidationResult validateClass(String code, String className, AstRules rules) {
        AstValidationResult result = new AstValidationResult();
        if (rules == null || rules.getClassRules() == null) return result;
        for (ClassRule r : rules.getClassRules()) {
            if (r.getClassName().equals(className)) {
                result.merge(classRuleChecker.check(code, r));
            }
        }
        return result;
    }
}
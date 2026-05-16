package com.example.runner.service.module3.reflection;

import com.example.runner.dto.module3.ReflectionRules;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Валидатор скомпилированного кода через Java Reflection API
 *
 * Проверяет наличие классов, иерархию наследования, модификаторы
 * методов, полей и конструкторов согласно {@link ReflectionRules}.
 */
public class ReflectionValidator {

    /** ClassLoader с загруженными классами студента и scaffold */
    private final ClassLoader classLoader;
    /** Список FQCN всех скомпилированных классов */
    private final List<String> allClasses;

    /**
     * @param classLoader загрузчик скомпилированных классов
     * @param allClasses  полные имена всех классов в артефакте компиляции
     */
    public ReflectionValidator(ClassLoader classLoader, List<String> allClasses) {
        this.classLoader = classLoader;
        this.allClasses = allClasses != null ? allClasses : List.of();
    }

    /**
     * Проверяет скомпилированный код по правилам рефлексии
     * @param rules правила рефлексивной проверки; при null возвращается успех
     * @return результат с ошибками и предупреждениями
     */
    public ReflectionValidationResult validate(ReflectionRules rules) {
        ReflectionValidationResult result = new ReflectionValidationResult();

        if (rules == null) {
            return result;
        }

        if (rules.getRequiredClasses() != null) {
            for (String className : rules.getRequiredClasses()) {
                try {
                    findClassByName(className);
                } catch (ClassNotFoundException e) {
                    result.addError("Класс не найден: " + className);
                }
            }
        }

        if (rules.getRequiredInterfaces() != null) {
            for (String interfaceName : rules.getRequiredInterfaces()) {
                try {
                    findClassByName(interfaceName);
                } catch (ClassNotFoundException e) {
                    result.addError("Интерфейс не найден: " + interfaceName);
                }
            }
        }

        if (rules.getExtendsRules() != null) {
            for (String rule : rules.getExtendsRules()) {
                String[] parts = rule.split(":");
                if (parts.length == 2) {
                    checkExtends(result, parts[0], parts[1]);
                }
            }
        }
        
        if (rules.getImplementsRules() != null) {
            for (String rule : rules.getImplementsRules()) {
                String[] parts = rule.split(":");
                if (parts.length == 2) {
                    checkImplements(result, parts[0], parts[1]);
                }
            }
        }

        if (rules.getPublicMethods() != null) {
            for (String rule : rules.getPublicMethods()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String methodName = rule.substring(lastDot + 1);
                    checkMethodModifier(result, className, methodName, Modifier.PUBLIC);
                }
            }
        }

        if (rules.getPrivateMethods() != null) {
            for (String rule : rules.getPrivateMethods()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String methodName = rule.substring(lastDot + 1);
                    checkMethodModifier(result, className, methodName, Modifier.PRIVATE);
                }
            }
        }

        if (rules.getProtectedMethods() != null) {
            for (String rule : rules.getProtectedMethods()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String methodName = rule.substring(lastDot + 1);
                    checkMethodModifier(result, className, methodName, Modifier.PROTECTED);
                }
            }
        }

        if (rules.getPrivateFields() != null) {
            for (String rule : rules.getPrivateFields()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String fieldName = rule.substring(lastDot + 1);
                    checkFieldModifier(result, className, fieldName, Modifier.PRIVATE);
                }
            }
        }

        if (rules.getPublicFields() != null) {
            for (String rule : rules.getPublicFields()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String fieldName = rule.substring(lastDot + 1);
                    checkFieldModifier(result, className, fieldName, Modifier.PUBLIC);
                }
            }
        }

        if (rules.getProtectedFields() != null) {
            for (String rule : rules.getProtectedFields()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String fieldName = rule.substring(lastDot + 1);
                    checkFieldModifier(result, className, fieldName, Modifier.PROTECTED);
                }
            }
        }

        if (rules.getStaticFinalFields() != null) {
            for (String rule : rules.getStaticFinalFields()) {
                int lastDot = rule.lastIndexOf('.');
                if (lastDot > 0) {
                    String className = rule.substring(0, lastDot);
                    String fieldName = rule.substring(lastDot + 1);
                    checkFieldModifier(result, className, fieldName, Modifier.STATIC | Modifier.FINAL);
                }
            }
        }

        if (rules.getProtectedConstructors() != null) {
            for (String className : rules.getProtectedConstructors()) {
                checkConstructorModifier(result, className, Modifier.PROTECTED);
            }
        }
        
        if (rules.getAbstractClasses() != null) {
            for (String className : rules.getAbstractClasses()) {
                checkClassModifier(result, className, Modifier.ABSTRACT);
            }
        }
        
        if (rules.getFinalClasses() != null) {
            for (String className : rules.getFinalClasses()) {
                checkClassModifier(result, className, Modifier.FINAL);
            }
        }
        
        return result;
    }
    
    /** Проверяет, что класс наследуется от указанного родителя */
    private void checkExtends(ReflectionValidationResult result, String className, String parentClass) {
        try {
            Class<?> clazz = findClassByName(className);
            Class<?> parent = findClassByName(parentClass);
            if (!parent.isAssignableFrom(clazz) || clazz == parent) {
                result.addError("Класс " + className + " не наследуется от " + parentClass);
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + (e.getMessage()));
        }
    }

    /** Проверяет, что класс реализует указанный интерфейс */
    private void checkImplements(ReflectionValidationResult result, String className, String interfaceName) {
        try {
            Class<?> clazz = findClassByName(className);
            Class<?> iface = findClassByName(interfaceName);
            if (!iface.isInterface() || !iface.isAssignableFrom(clazz)) {
                result.addError("Класс " + className + " не реализует интерфейс " + interfaceName);
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + (e.getMessage()));
        }
    }

    /** Проверяет модификатор доступа метода */
    private void checkMethodModifier(ReflectionValidationResult result, String className, String methodName, int expectedModifier) {
        try {
            Class<?> clazz = findClassByName(className);
            Method[] methods = clazz.getDeclaredMethods();
            boolean found = false;
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    found = true;
                    if (!hasModifier(method.getModifiers(), expectedModifier)) {
                        result.addError("Метод " + methodName + " в классе " + className + " должен иметь модификатор " + getModifierName(expectedModifier));
                    }
                }
            }
            if (!found) {
                result.addError("Метод " + methodName + " не найден в классе " + className);
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + className);
        }
    }

    /** Проверяет модификатор доступа поля */
    private void checkFieldModifier(ReflectionValidationResult result, String className, String fieldName, int expectedModifier) {
        try {
            Class<?> clazz = findClassByName(className);
            Field[] fields = clazz.getDeclaredFields();
            boolean found = false;
            for (Field field : fields) {
                if (field.getName().equals(fieldName)) {
                    found = true;
                    if (!hasModifier(field.getModifiers(), expectedModifier)) {
                        result.addError("Поле " + fieldName + " в классе " + className + " должно иметь модификатор " + getModifierName(expectedModifier));
                    }
                }
            }
            if (!found) {
                result.addError("Поле " + fieldName + " не найдено в классе " + className);
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + className);
        }
    }

    /** Проверяет модификатор доступа конструктора */
    private void checkConstructorModifier(ReflectionValidationResult result, String className, int expectedModifier) {
        try {
            Class<?> clazz = findClassByName(className);
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            boolean found = false;
            for (Constructor<?> constructor : constructors) {
                if (constructor.getDeclaringClass() == clazz) {
                    found = true;
                    if (!hasModifier(constructor.getModifiers(), expectedModifier)) {
                        result.addError("Конструктор класса " + className + " должен иметь модификатор " + getModifierName(expectedModifier));
                    }
                }
            }
            if (!found) {
                result.addError("Конструктор не найден в классе " + className);
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + className);
        }
    }
    
    /** Проверяет модификатор класса (abstract, final) */
    private void checkClassModifier(ReflectionValidationResult result, String className, int expectedModifier) {
        try {
            Class<?> clazz = findClassByName(className);
            if (!hasModifier(clazz.getModifiers(), expectedModifier)) {
                result.addError("Класс " + className + " должен иметь модификатор " + getModifierName(expectedModifier));
            }
        } catch (ClassNotFoundException e) {
            result.addError("Класс не найден: " + className);
        }
    }


    /**
     * Загружает класс по полному или короткому имени
     * @param className FQCN или короткое имя класса
     * @return загруженный класс
     * @throws ClassNotFoundException если класс не найден
     */
    private Class<?> findClassByName(String className) throws ClassNotFoundException {
        if (className.contains(".")) {
            return classLoader.loadClass(className);
        }
        
        for (String fqcn : allClasses) {
            String simpleName = fqcn.substring(fqcn.lastIndexOf('.') + 1);
            if (simpleName.equals(className)) {
                return classLoader.loadClass(fqcn);
            }
        }
        
        return classLoader.loadClass(className);
    }
    
    /** @return true, если битовая маска содержит ожидаемый модификатор */
    private boolean hasModifier(int modifiers, int expectedModifier) {
        return (modifiers & expectedModifier) != 0;
    }
    
    /** @return текстовое представление модификатора для сообщений об ошибках */
    private String getModifierName(int modifier) {
        if (modifier == Modifier.PUBLIC) return "public";
        if (modifier == Modifier.PROTECTED) return "protected";
        if (modifier == Modifier.PRIVATE) return "private";
        if (modifier == Modifier.ABSTRACT) return "abstract";
        if (modifier == Modifier.FINAL) return "final";
        if (modifier == Modifier.STATIC) return "static";
        if (modifier == (Modifier.STATIC | Modifier.FINAL)) return "static final";
        return "unknown";
    }
}

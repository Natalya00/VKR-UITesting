package com.example.runner.service.module3;

import com.example.runner.dto.module3.*;
import com.example.runner.service.module3.ast.AstValidationResult;
import com.example.runner.service.module3.ast.AstValidator;
import com.example.runner.service.module3.harness.HarnessCompiler;
import com.example.runner.service.module3.harness.HarnessRunner;
import com.example.runner.service.module3.reflection.ReflectionValidationResult;
import com.example.runner.service.module3.reflection.ReflectionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Валидация POM-кода модуля 3
 *
 * Последовательно выполняет этапы: AST-проверка исходников, компиляция
 * с scaffold-классами, рефлексивная проверка байткода и запуск harness-тестов.
 */
public class Module3ValidationPipeline {

    private static final Logger log = LoggerFactory.getLogger(Module3ValidationPipeline.class);

    /** Валидатор структуры исходного кода через JavaParser */
    private final AstValidator    astValidator;
    /** Компилятор студенческого кода вместе со scaffold */
    private final HarnessCompiler harnessCompiler;
    /** Запускает harness-тесты после компиляции */
    private final HarnessRunner   harnessRunner;

    /** Создаёт конвейер с компонентами по умолчанию */
    public Module3ValidationPipeline() {
        this.astValidator    = new AstValidator();
        this.harnessCompiler = new HarnessCompiler();
        this.harnessRunner   = new HarnessRunner();
    }


    /**
     * Результат прохождения конвейера валидации
     */
    public static class PipelineResult {
        private final boolean      success;
        private final List<String> errors;
        private final List<String> warnings;

        /**
         * @param success  признак успешной валидации
         * @param errors   список ошибок
         * @param warnings список предупреждений
         */
        public PipelineResult(boolean success, List<String> errors, List<String> warnings) {
            this.success  = success;
            this.errors   = errors   != null ? errors   : List.of();
            this.warnings = warnings != null ? warnings : List.of();
        }

        /** @return true, если все этапы валидации пройдены */
        public boolean      isSuccess()         { return success; }
        /** @return список сообщений об ошибках */
        public List<String> getErrors()         { return errors; }
        /** @return список предупреждений */
        public List<String> getWarnings()       { return warnings; }
        /** @return первое сообщение об ошибке или null */
        public String       getFirstError()     { return errors.isEmpty() ? null : errors.get(0); }
        /** @return все ошибки, объединённые переводом строки */
        public String       getErrorsAsString() { return String.join("\n", errors); }
    }

    /**
     * Выполняет валидацию решения
     * @param studentInput код: строка или {@code Map<имя файла, содержимое>}
     * @param rules        правила валидации из конфигурации упражнения
     * @param baseUrl      базовый URL фронтенда для Selenide/JUnit-тестов
     * @return результат с ошибками, предупреждениями и признаком успеха
     */
    public PipelineResult validate(Object studentInput, Module3ValidationRules rules, String baseUrl) {
        log.info("[Module3ValidationPipeline] Начало валидации...");
        
        List<String> allWarnings = new ArrayList<>();
        HarnessCompiler.CompilationResult compilationResult = null;

        Collection<String> codeFiles = extractCodeFiles(studentInput);
        log.info("[Module3ValidationPipeline] Получено файлов: {}", codeFiles.size());

        if (codeFiles.isEmpty()) {
            log.error("[Module3ValidationPipeline] Код не передан");
            return new PipelineResult(false,
                    List.of("Код не передан — ни поле code, ни files не заполнены"),
                    allWarnings);
        }

        try {
            log.info("[Module3ValidationPipeline] Шаг 1: AST валидация...");
            if (rules.getAstRules() != null) {
                AstValidationResult combined = new AstValidationResult();
                
                java.util.Set<String> foundClasses = new java.util.HashSet<>();
                
                log.info("[Module3ValidationPipeline] Проверяю {} файлов(а)", codeFiles.size());
                
                for (String code : codeFiles) {
                    try {
                        com.github.javaparser.ast.CompilationUnit cu = 
                            com.github.javaparser.StaticJavaParser.parse(code);
                        cu.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)
                          .forEach(cls -> {
                              foundClasses.add(cls.getNameAsString());
                              log.info("[Module3ValidationPipeline] Найден класс: {} в файле", cls.getNameAsString());
                          });
                    } catch (Exception e) {
                        log.error("[Module3ValidationPipeline] Ошибка парсинга файла: {}", e.getMessage());
                    }
                    
                    AstValidationResult fileResult = astValidator.validate(code, rules.getAstRules());
                    combined.merge(fileResult);
                }
                
                log.info("[Module3ValidationPipeline] Все найденные классы: {}", foundClasses);
                
                if (rules.getAstRules().getClassRules() != null) {
                    for (com.example.runner.dto.module3.ClassRule rule : rules.getAstRules().getClassRules()) {
                        String className = simpleName(rule.getClassName());
                        log.info("[Module3ValidationPipeline] Проверяю required класс: {}, required={}, найден={}", 
                            className, rule.isRequired(), foundClasses.contains(className));
                        
                        if (rule.isRequired()) {
                            if (!foundClasses.contains(className)) {
                                log.error("[Module3ValidationPipeline] Класс {} НЕ НАЙДЕН", className);
                                combined.addError(rule.getErrorMessage() != null 
                                    ? rule.getErrorMessage() 
                                    : "Класс " + className + " не найден");
                            } else {
                                log.info("[Module3ValidationPipeline] Класс {} найден", className);
                            }
                        }
                    }
                }
                
                allWarnings.addAll(combined.getWarnings());
                if (!combined.isSuccess()) {
                    log.error("[Module3ValidationPipeline] AST валидация не пройдена: {}", combined.getErrors());
                    return new PipelineResult(false, combined.getErrors(), allWarnings);
                }
                log.info("[Module3ValidationPipeline] AST валидация пройдена");
            }

            log.info("[Module3ValidationPipeline] Шаг 2: Компиляция...");
            if (rules.getHarnessRules() != null) {
                List<String> scaffoldClasses = resolveScaffoldClasses(rules.getHarnessRules());
                log.info("[Module3ValidationPipeline] Scaffold классы: {}", scaffoldClasses);
                compilationResult = harnessCompiler.compile(studentInput, scaffoldClasses);

                if (!compilationResult.isSuccess()) {
                    log.error("[Module3ValidationPipeline] Компиляция не удалась: {}", compilationResult.getError());
                    return new PipelineResult(
                            false,
                            List.of(compilationResult.getError()),
                            allWarnings);
                }
                log.info("[Module3ValidationPipeline] Компиляция успешна: {}", compilationResult.getClassesDir());
            }

            log.info("[Module3ValidationPipeline] Шаг 3: Reflection валидация...");
            if (rules.getReflectionRules() != null
                    && compilationResult != null
                    && compilationResult.getClassesDir() != null) {

                try (URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{compilationResult.getClassesDir().toUri().toURL()},
                        Module3ValidationPipeline.class.getClassLoader())) {

                    ReflectionValidator rv = new ReflectionValidator(classLoader, compilationResult.getAllClasses());
                    ReflectionValidationResult rvResult = rv.validate(rules.getReflectionRules());

                    allWarnings.addAll(rvResult.getWarnings());
                    if (!rvResult.isSuccess()) {
                        log.error("[Module3ValidationPipeline] Reflection валидация не пройдена: {}", rvResult.getErrors());
                        return new PipelineResult(false, rvResult.getErrors(), allWarnings);
                    }
                    log.info("[Module3ValidationPipeline] Reflection валидация пройдена");
                }
            }

            log.info("[Module3ValidationPipeline] Шаг 4: Harness тесты...");
            if (rules.getHarnessRules() != null
                    && compilationResult != null
                    && compilationResult.getClassesDir() != null
                    && !rules.getHarnessRules().isCompileOnly()) {

                log.info("[Module3ValidationPipeline] Запуск HarnessRunner...");
                HarnessRunner.HarnessResult hr = harnessRunner.run(
                        compilationResult.getClassesDir(),
                        rules.getHarnessRules(),
                        baseUrl);

                if (!hr.isSuccess()) {
                    log.error("[Module3ValidationPipeline] Harness тесты не пройдены: {}", hr.getError());
                    return new PipelineResult(false, List.of(hr.getError()), allWarnings);
                }
                log.info("[Module3ValidationPipeline] Harness тесты пройдены: {}", hr.getOutput());
            }

            log.info("[Module3ValidationPipeline] Валидация завершена успешно");
            return new PipelineResult(true, List.of(), allWarnings);

        } catch (Exception e) {
            log.error("[Module3ValidationPipeline] Внутренняя ошибка: {}", e.getMessage(), e);
            return new PipelineResult(
                    false,
                    List.of("Внутренняя ошибка валидации: " + e.getMessage()),
                    allWarnings);
        } finally {
            if (compilationResult != null) {
                compilationResult.cleanup();
            }
        }
    }


    /**
     * Извлекает короткое имя класса из полного (FQCN)
     * @param name полное или короткое имя класса
     * @return имя без пакета
     */
    private String simpleName(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }


    /**
     * Извлекает коллекцию исходных файлов из входных данных студента
     * @param studentInput строка с кодом или карта файлов
     * @return коллекция содержимого файлов; пустая, если формат не поддерживается
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractCodeFiles(Object studentInput) {
        if (studentInput instanceof String code) {
            return List.of(code);
        }
        if (studentInput instanceof Map<?, ?> map) {
            return (Collection<String>) ((Map<String, String>) map).values();
        }
        return List.of();
    }

    /**
     * Возвращает список имён scaffold-классов из правил harness
     * @param harnessRules правила запуска harness
     * @return список имён классов или пустой список
     */
    private List<String> resolveScaffoldClasses(HarnessRules harnessRules) {
        if (harnessRules.getScaffoldClasses() == null) return List.of();
        return Arrays.asList(harnessRules.getScaffoldClasses());
    }


    /**
     * Выполняет полный конвейер с базовым URL по умолчанию
     * @param studentInput код
     * @param rules        правила валидации
     * @return результат валидации
     */
    public PipelineResult validate(Object studentInput, Module3ValidationRules rules) {
        return validate(studentInput, rules, "http://localhost:5173");
    }

    /**
     * Выполняет только AST-валидацию без компиляции и harness
     * @param studentCode исходный код
     * @param astRules    правила AST-проверки
     * @return результат валидации
     */
    public PipelineResult validateAstOnly(String studentCode, AstRules astRules) {
        AstValidationResult result = astValidator.validate(studentCode, astRules);
        return new PipelineResult(result.isSuccess(), result.getErrors(), result.getWarnings());
    }

    /**
     * Проверяет только компиляцию кода со scaffold-классами
     * @param studentCode      исходный код
     * @param scaffoldClasses  имена scaffold-классов для подключения
     * @return результат с сообщением об успехе или ошибке компиляции
     */
    public PipelineResult validateCompileOnly(String studentCode, List<String> scaffoldClasses) {
        HarnessCompiler.CompilationResult result =
                harnessCompiler.compile(studentCode, scaffoldClasses);
        try {
            return result.isSuccess()
                    ? new PipelineResult(true,  List.of(), List.of("Компиляция успешна"))
                    : new PipelineResult(false, List.of(result.getError()), List.of());
        } finally {
            result.cleanup();
        }
    }
}
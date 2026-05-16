package com.example.runner.service.module3.harness;

import com.example.runner.dto.module3.HarnessRules;
import com.example.runner.harness.module3.AbstractHarnessTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Запускает harness-тесты для скомпилированного POM-кода
 *
 * Поддерживает три режима: JUnit Platform, вызов через рефлексию
 * и отдельный JVM-процесс (Selenide).
 */
public class HarnessRunner {

    private static final Logger log = LoggerFactory.getLogger(HarnessRunner.class);
    /** Таймаут по умолчанию для процессного режима, секунды */
    private static final long DEFAULT_TIMEOUT_SECONDS = 120;

    /**
     * Результат выполнения harness
     */
    public static class HarnessResult {
        private final boolean success;
        private final String  error;
        private final String  output;

        private HarnessResult(boolean success, String error, String output) {
            this.success = success;
            this.error   = error;
            this.output  = output;
        }

        /** @param output вывод успешного выполнения */
        public static HarnessResult success(String output) { return new HarnessResult(true, null, output); }
        /** @param error сообщение об ошибке */
        public static HarnessResult failure(String error)  { return new HarnessResult(false, error, null); }

        /** @return true, если harness выполнен успешно */
        public boolean isSuccess() { return success; }
        /** @return текст ошибки или null */
        public String  getError()  { return error; }
        /** @return stdout или итоговое сообщение */
        public String  getOutput() { return output; }
    }

    /**
     * Запускает harness в режиме, заданном в {@link HarnessRules}
     * @param classesDir каталог со скомпилированными классами
     * @param rules      правила запуска harness
     * @param baseUrl    базовый URL приложения для UI-тестов
     * @return результат выполнения
     */
    public HarnessResult run(Path classesDir, HarnessRules rules, String baseUrl) {
        if (rules.isCompileOnly()) {
            return HarnessResult.success("Компиляция успешна");
        }
        if (rules.isJUnitTest()) {
            return runJUnitTests(classesDir, rules, baseUrl);
        }
        if (rules.isSelenide()) {
            return runAsProcess(classesDir, rules);
        }
        return runViaReflection(classesDir, rules);
    }

    /** Запускает JUnit тестов через Platform Launcher */
    private HarnessResult runJUnitTests(Path classesDir, HarnessRules rules, String baseUrl) {
        log.info("[HarnessRunner] Запуск JUnit-тестов для упражнения...");
        log.info("[HarnessRunner] classesDir: {}", classesDir.toAbsolutePath());
        log.info("[HarnessRunner] classesDir существует: {}", Files.exists(classesDir));
        log.info("[HarnessRunner] testClass: {}", rules.getTestClass());
        log.info("[HarnessRunner] testMethods: {}", rules.getTestMethods());
        log.info("[HarnessRunner] baseUrl: {}", baseUrl);

        try {
            log.info("[HarnessRunner] Содержимое classesDir:");
            Files.walk(classesDir).forEach(path -> {
                log.info("  - {}", classesDir.relativize(path));
            });
        } catch (IOException e) {
            log.warn("[HarnessRunner] Не удалось прочитать classesDir: {}", e.getMessage());
        }

        if (rules.getTestClass() == null || rules.getTestClass().isBlank()) {
            return HarnessResult.failure("jUnitTest=true но testClass не указан");
        }

        try {
            ClassLoader appClassLoader = HarnessRunner.class.getClassLoader();
            log.info("[HarnessRunner] App ClassLoader: {}", appClassLoader.getClass().getName());

            List<URL> urls = new ArrayList<>();

            URL classesDirUrl = classesDir.toUri().toURL();
            urls.add(classesDirUrl);
            log.info("[HarnessRunner] Добавлен classesDir: {}", classesDirUrl);

            URL appLocation = HarnessRunner.class.getProtectionDomain().getCodeSource().getLocation();
            urls.add(appLocation);
            log.info("[HarnessRunner] Добавлен app location: {}", appLocation);

            try (URLClassLoader testClassLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    appClassLoader)) {

                log.info("[HarnessRunner] testClassLoader: {}", testClassLoader.getClass().getName());
                log.info("[HarnessRunner] Parent ClassLoader: {}", testClassLoader.getParent());

                try {
                    AbstractHarnessTest.setStudentClassLoader(testClassLoader);
                    log.info("[HarnessRunner] studentClassLoader установлен в AbstractHarnessTest");
                } catch (Exception e) {
                    log.warn("[HarnessRunner] Не удалось установить studentClassLoader в AbstractHarnessTest: {}", e.getMessage());
                }

                if (baseUrl != null && !baseUrl.isBlank()) {
                    System.setProperty("baseUrl", baseUrl);
                    log.info("[HarnessRunner] System.setProperty('baseUrl', '{}') установлен", baseUrl);
                }

                log.info("[HarnessRunner] Проверка видимости классов студента...");
                String studentClassName = "com.example.runner.pom.elements.Input";
                URL studentClassUrl = testClassLoader.getResource(studentClassName.replace('.', '/') + ".class");
                log.info("[HarnessRunner] Ресурс {} = {}", studentClassName, studentClassUrl);

                Class<?> testClass;
                try {
                    testClass = testClassLoader.loadClass(rules.getTestClass());
                    log.info("[HarnessRunner] Тест-класс загружен: {}", testClass.getName());
                    log.info("[HarnessRunner] Тест-класс location: {}", testClass.getProtectionDomain().getCodeSource().getLocation());
                } catch (ClassNotFoundException e) {
                    log.error("[HarnessRunner] Не удалось загрузить тест-класс: {}", rules.getTestClass());
                    log.error("[HarnessRunner] Доступные ресурсы в ClassLoader:");
                    var resources = testClassLoader.getResources(rules.getTestClass().replace('.', '/') + ".class");
                    while (resources.hasMoreElements()) {
                        log.error("  - {}", resources.nextElement());
                    }
                    throw e;
                }

                Launcher launcher = LauncherFactory.create();

                LauncherDiscoveryRequest request;
                if (rules.getTestMethods() != null && rules.getTestMethods().size() > 0) {
                    log.info("[HarnessRunner] Запуск конкретных методов: {}", rules.getTestMethods());
                    List<MethodSelector> methodSelectors = new ArrayList<>();
                    for (String m : rules.getTestMethods()) {
                        methodSelectors.add(DiscoverySelectors.selectMethod(testClass, m));
                    }
                    request = LauncherDiscoveryRequestBuilder.request()
                        .selectors(methodSelectors)
                        .build();
                } else {
                    log.info("[HarnessRunner] Запуск всех методов в классе");
                    request = LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClass(testClass))
                        .build();
                }

                SummaryGeneratingListener listener = new SummaryGeneratingListener();

                log.info("[HarnessRunner] Запуск тестов...");
                launcher.execute(request, listener);

                TestExecutionSummary summary = listener.getSummary();
                long totalTests = summary.getTestsStartedCount();
                long failures = summary.getFailures().size();

                log.info("[HarnessRunner] Всего тестов: {}, Провалено: {}", totalTests, failures);

                if (failures > 0) {
                    StringBuilder errors = new StringBuilder();
                    errors.append("Тестов провалено: ").append(failures).append(" из ").append(totalTests).append("\n");
                    for (TestExecutionSummary.Failure failure : summary.getFailures()) {
                        String errorMsg = failure.getException().getMessage();
                        log.error("[HarnessRunner] FAILURE: {}", errorMsg);
                        errors.append(failure.getException().getClass().getSimpleName())
                              .append(": ")
                              .append(errorMsg)
                              .append("\n");
                    }
                    return HarnessResult.failure(errors.toString());
                }

                log.info("[HarnessRunner] Все тесты пройдены успешно!");
                return HarnessResult.success("Тестов пройдено: " + totalTests);
            }

        } catch (ClassNotFoundException e) {
            log.error("[HarnessRunner] ClassNotFoundException: {}", e.getMessage());
            return HarnessResult.failure("Тест-класс не найден: " + rules.getTestClass() +
                "\nПроверьте что harness-тесты скомпилированы и доступны в classpath");
        } catch (Exception e) {
            log.error("[HarnessRunner] Exception: {}", e.getMessage(), e);
            return HarnessResult.failure("Ошибка запуска JUnit-тестов: " + e.getMessage());
        }
    }

    /** Вызывает точку входа (main/run) через рефлексию в том же JVM */
    private HarnessResult runViaReflection(Path classesDir, HarnessRules rules) {
        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesDir.toFile().toURI().toURL()},
                HarnessRunner.class.getClassLoader())) {

            String entryClass  = rules.getEntryPointClass()  != null ? rules.getEntryPointClass()  : "UserScript";
            String entryMethod = rules.getEntryPointMethod() != null ? rules.getEntryPointMethod() : "run";

            Class<?> userClass = classLoader.loadClass(entryClass);
            Method method = findMethod(userClass, entryMethod);

            if (method == null) {
                return HarnessResult.failure(
                        "Метод " + entryMethod + " не найден в классе " + entryClass);
            }

            Object result = invokeMethod(method, userClass);
            return HarnessResult.success(result != null ? result.toString() : "Выполнено успешно");

        } catch (ClassNotFoundException e) {
            return HarnessResult.failure("Класс не найден: " + e.getMessage());
        } catch (Exception e) {
            if (rules.getExpectedExceptions() != null) {
                Throwable root = e.getCause() != null ? e.getCause() : e;
                for (String expected : rules.getExpectedExceptions()) {
                    if (root.getClass().getName().equals(expected)
                            || root.getClass().getSimpleName().equals(expected)) {
                        return HarnessResult.success("Ожидаемое исключение получено: " + expected);
                    }
                }
            }
            Throwable root = e.getCause() != null ? e.getCause() : e;
            return HarnessResult.failure(root.getClass().getSimpleName() + ": " + root.getMessage());
        }
    }

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                if ("main".equals(methodName)
                        && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                return m;
            }
        }
        return null;
    }

    private Object invokeMethod(Method method, Class<?> clazz) throws Exception {
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            return method.getParameterCount() == 0
                    ? method.invoke(null)
                    : method.invoke(null, (Object) new String[0]);
        }
        Object instance = clazz.getDeclaredConstructor().newInstance();
        if (method.getParameterCount() == 0) return method.invoke(instance);
        throw new UnsupportedOperationException(
                "Методы с параметрами не поддерживаются в Reflection-режиме");
    }

    /** Запускает отдельный java-процесс с classpath (режим Selenide) */
    private HarnessResult runAsProcess(Path classesDir, HarnessRules rules) {
        try {
            String classpath = buildProcessClasspath(classesDir);
            String entryClass = rules.getEntryPointClass() != null
                    ? rules.getEntryPointClass() : "HarnessTest";
            long timeoutSec = rules.getTimeoutMs() > 0
                    ? rules.getTimeoutMs() / 1000
                    : DEFAULT_TIMEOUT_SECONDS;

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(classpath);
            command.add(entryClass);
            if (rules.getProcessArgs() != null) command.addAll(rules.getProcessArgs());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);

            Process process = pb.start();
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            Thread t1 = new Thread(() -> readStream(process.getInputStream(), stdout));
            Thread t2 = new Thread(() -> readStream(process.getErrorStream(), stderr));
            t1.start();
            t2.start();

            boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            t1.join(2000);
            t2.join(2000);

            if (!finished) {
                process.destroyForcibly();
                return HarnessResult.failure(
                        "Превышено время выполнения теста (" + timeoutSec + "с)");
            }

            int exitCode = process.exitValue();
            String out = stdout.toString();

            if (exitCode == 0 || out.contains("__RUN_SUCCESS__")) {
                return HarnessResult.success(out);
            } else {
                String errMsg = stderr.toString();
                return HarnessResult.failure(errMsg.isBlank() ? out : errMsg);
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return HarnessResult.failure("Ошибка запуска процесса: " + e.getMessage());
        }
    }


    private String buildProcessClasspath(Path classesDir) {
        return buildProcessClasspath(classesDir, true);
    }

    private String buildProcessClasspath(Path classesDir, boolean includeAppClasses) {
        List<String> entries = new ArrayList<>();
        entries.add(classesDir.toAbsolutePath().toString());

        if (includeAppClasses) {
            String appClassesDir = getAppClassesDirectory();
            if (appClassesDir != null && !appClassesDir.isBlank()) {
                entries.add(appClassesDir);
            } else {
                String cp = System.getProperty("java.class.path");
                if (cp != null && !cp.isBlank()) {
                    for (String entry : cp.split(File.pathSeparator)) {
                        if (!entries.contains(entry)) {
                            entries.add(entry);
                        }
                    }
                }
            }
        }

        String rcp = System.getProperty("java.class.path");
        if (rcp != null && !rcp.isBlank()) {
            for (String entry : rcp.split(File.pathSeparator)) {
                if (!entries.contains(entry)) {
                    entries.add(entry);
                }
            }
        }

        Path libDir = Path.of("/app/lib");
        if (Files.exists(libDir) && Files.isDirectory(libDir)) {
            try (var stream = Files.list(libDir)) {
                stream.filter(p -> p.toString().endsWith(".jar"))
                      .forEach(p -> entries.add(p.toAbsolutePath().toString()));
            } catch (IOException ignored) {}
        }

        return String.join(File.pathSeparator, entries);
    }

    private String getAppClassesDirectory() {
        String appClasses = System.getProperty("app.classes.dir");
        if (appClasses != null && !appClasses.isBlank() && Files.exists(Path.of(appClasses))) {
            return Path.of(appClasses).toAbsolutePath().toString();
        }
        
        try {
            String classLocation = HarnessRunner.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            
            if (classLocation == null) return null;
            
            classLocation = java.net.URLDecoder.decode(classLocation, StandardCharsets.UTF_8.name());
            Path location = Path.of(classLocation);
            
            if (classLocation.endsWith(".jar")) {
                return location.toAbsolutePath().toString();
            }
            
            if (Files.isDirectory(location)) {
                Path harnessTestClass = location.resolve("com/example/runner/harness/module3/AbstractHarnessTest.class");
                if (Files.exists(harnessTestClass)) {
                    return location.toAbsolutePath().toString();
                }
                
                Path parent = location.getParent();
                if (parent != null && Files.isDirectory(parent)) {
                    Path parentHarnessTest = parent.resolve("com/example/runner/harness/module3/AbstractHarnessTest.class");
                    if (Files.exists(parentHarnessTest)) {
                        return parent.toAbsolutePath().toString();
                    }
                }
                
                Path currentDir = Path.of(System.getProperty("user.dir"));
                Path targetClasses = currentDir.resolve("target").resolve("classes");
                if (Files.exists(targetClasses)) {
                    Path tcHarnessTest = targetClasses.resolve("com/example/runner/harness/module3/AbstractHarnessTest.class");
                    if (Files.exists(tcHarnessTest)) {
                        return targetClasses.toAbsolutePath().toString();
                    }
                }
            }
            
        } catch (Exception e) {
            
        }
        
        return null;
    }

    private void readStream(java.io.InputStream is, StringBuilder sb) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException ignored) {}
    }
}
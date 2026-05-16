package com.example.runner.service.module3.harness;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Компилятор POM-кода вместе со scaffold-классами
 *
 * Создаёт временную директорию, копирует scaffold из classpath,
 * записывает код с учётом package и компилирует через {@link JavaCompiler}.
 */
public class HarnessCompiler {

    /** Путь к scaffold-файлам в resources */
    private static final String SCAFFOLD_PATH = "scaffold/module3/";
    /** Регулярное выражение для извлечения package из исходника */
    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    /**
     * Компилирует код со scaffold-классами
     * @param studentInput        строка с кодом или {@code Map<имя файла, содержимое>}
     * @param scaffoldClassNames  имена scaffold-классов без расширения .java
     * @return результат с путём к .class или текстом ошибки
     */
    public CompilationResult compile(Object studentInput, List<String> scaffoldClassNames) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("harness-compile-");
            Path sourceDir = Files.createDirectories(tempDir.resolve("src"));
            Path classesDir = Files.createDirectories(tempDir.resolve("classes"));

            List<Path> sourceFiles = new ArrayList<>();
            List<String> allClasses = new ArrayList<>(); 

            System.out.println("[HarnessCompiler] scaffoldClassNames: " + scaffoldClassNames);

            if (scaffoldClassNames != null) {
                for (String className : scaffoldClassNames) {
                    String resourceName = SCAFFOLD_PATH + className + ".java";
                    System.out.println("[HarnessCompiler] Читаю scaffold: " + resourceName);
                    String content = readResource(resourceName);
                    Path targetFile = resolvePackagePath(sourceDir, content, className + ".java");
                    Files.createDirectories(targetFile.getParent());
                    Files.writeString(targetFile, content, StandardCharsets.UTF_8);
                    sourceFiles.add(targetFile);
                    String packageName = extractPackage(content);
                    if (packageName != null && !packageName.isBlank()) {
                        allClasses.add(packageName + "." + className);
                        System.out.println("[HarnessCompiler] Scaffold класс: " + packageName + "." + className);
                    } else {
                        allClasses.add(className);
                        System.out.println("[HarnessCompiler] Scaffold класс: " + className);
                    }
                }
            }

            if (studentInput instanceof String studentCode) {
                String studentFileName = resolveClassName(studentCode) + ".java";
                Path studentFile = resolvePackagePath(sourceDir, studentCode, studentFileName);
                Files.createDirectories(studentFile.getParent());
                Files.writeString(studentFile, studentCode, StandardCharsets.UTF_8);
                sourceFiles.add(studentFile);
                String packageName = extractPackage(studentCode);
                String className = resolveClassName(studentCode);
                if (packageName != null && !packageName.isBlank()) {
                    allClasses.add(packageName + "." + className);
                    System.out.println("[HarnessCompiler] Студент класс: " + packageName + "." + className);
                } else {
                    allClasses.add(className);
                    System.out.println("[HarnessCompiler] Студент класс: " + className);
                }
            } else if (studentInput instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> studentFiles = (Map<String, String>) studentInput;
                System.out.println("[HarnessCompiler] Студент файлов: " + studentFiles.size());
                for (Map.Entry<String, String> entry : studentFiles.entrySet()) {
                    String fileName = entry.getKey();
                    String code = entry.getValue();
                    System.out.println("[HarnessCompiler] Файл: " + fileName + ", код (первые 100 симв): " + code.substring(0, Math.min(100, code.length())));
                    Path targetFile = resolvePackagePath(sourceDir, code, fileName);
                    Files.createDirectories(targetFile.getParent());
                    Files.writeString(targetFile, code, StandardCharsets.UTF_8);
                    sourceFiles.add(targetFile);
                    String packageName = extractPackage(code);
                    String className = resolveClassName(code);
                    if (packageName != null && !packageName.isBlank()) {
                        allClasses.add(packageName + "." + className);
                        System.out.println("[HarnessCompiler] Студент класс: " + packageName + "." + className);
                    } else {
                        allClasses.add(className);
                        System.out.println("[HarnessCompiler] Студент класс: " + className);
                    }
                }
            } else {
                return CompilationResult.failure("Неверный формат studentInput: String или Map<String,String> ожидается");
            }

            System.out.println("[HarnessCompiler] Всего классов: " + allClasses);
            System.out.println("[HarnessCompiler] sourceFiles: " + sourceFiles.size());

            String error = compileSources(sourceFiles, classesDir);
            if (error != null) {
                System.out.println("[HarnessCompiler] Ошибка компиляции: " + error);
                deleteRecursively(tempDir);
                return CompilationResult.failure(error);
            }

            System.out.println("[HarnessCompiler] Компиляция успешна");
            return CompilationResult.success(classesDir, tempDir, allClasses);

        } catch (IOException e) {
            if (tempDir != null) deleteRecursively(tempDir);
            System.out.println("[HarnessCompiler] IOException: " + e.getMessage());
            return CompilationResult.failure("Ошибка при компиляции: " + e.getMessage());
        }
    }

    private Path resolvePackagePath(Path sourceDir, String code, String fileName) {
        String packageName = extractPackage(code);
        if (packageName == null || packageName.isBlank()) {
            return sourceDir.resolve(fileName);
        }
        String packagePath = packageName.replace('.', File.separatorChar);
        return sourceDir.resolve(packagePath).resolve(fileName);
    }

    private String extractPackage(String code) {
        Matcher m = PACKAGE_PATTERN.matcher(code);
        return m.find() ? m.group(1) : null;
    }

    private String resolveClassName(String code) {
        Pattern classPattern = Pattern.compile(
                "public\\s+(?:class|interface|enum|abstract\\s+class)\\s+(\\w+)");
        Matcher m = classPattern.matcher(code);
        return m.find() ? m.group(1) : "StudentCode";
    }

    private String readResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Scaffold-класс не найден: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String compileSources(List<Path> sourceFiles, Path classesDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return "JavaCompiler не найден. Убедитесь, что используется JDK, а не JRE.";
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics, null, StandardCharsets.UTF_8);

        try {
            Iterable<? extends JavaFileObject> units =
                    fileManager.getJavaFileObjectsFromPaths(sourceFiles);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    List.of(
                            "-d", classesDir.toString(),
                            "-classpath", buildClasspath(),
                            "-encoding", "UTF-8"
                    ),
                    null,
                    units
            );

            if (!task.call()) {
                StringBuilder errors = new StringBuilder("Ошибка компиляции:\n");
                for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR) {
                        errors.append("  ")
                              .append(d.getMessage(null))
                              .append(" в ")
                              .append(d.getSource() != null ? d.getSource().getName() : "unknown")
                              .append(":")
                              .append(d.getLineNumber())
                              .append("\n");
                    }
                }
                return errors.toString();
            }
            return null;

        } catch (Exception e) {
            return "Ошибка при компиляции: " + e.getMessage();
        } finally {
            try { fileManager.close(); } catch (IOException ignored) {}
        }
    }

    private String buildClasspath() {
        List<String> entries = new ArrayList<>();

        String runtimeCp = System.getProperty("java.class.path");
        if (runtimeCp != null && !runtimeCp.isBlank()) {
            entries.add(runtimeCp);
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

    void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (var stream = Files.list(path)) {
                    stream.forEach(this::deleteRecursively);
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

    /**
     * Результат компиляции harness
     */
    public static class CompilationResult {
        private final boolean success;
        private final String  error;
        private final Path    classesDir;
        private final Path    tempDir;
        private final List<String> allClasses;

        private CompilationResult(boolean success, String error,
                                   Path classesDir, Path tempDir, List<String> allClasses) {
            this.success    = success;
            this.error      = error;
            this.classesDir = classesDir;
            this.tempDir    = tempDir;
            this.allClasses = allClasses != null ? allClasses : List.of();
        }

        /**
         * @param classesDir каталог с скомпилированными .class
         * @param tempDir    корневая временная директория
         * @param allClasses список FQCN всех классов
         */
        public static CompilationResult success(Path classesDir, Path tempDir, List<String> allClasses) {
            return new CompilationResult(true, null, classesDir, tempDir, allClasses);
        }

        /** @param classesDir каталог с .class; @param tempDir временная директория */
        public static CompilationResult success(Path classesDir, Path tempDir) {
            return new CompilationResult(true, null, classesDir, tempDir, List.of());
        }

        /** @param error текст ошибки компиляции */
        public static CompilationResult failure(String error) {
            return new CompilationResult(false, error, null, null, List.of());
        }

        /** @return true при успешной компиляции */
        public boolean      isSuccess()      { return success; }
        /** @return сообщение об ошибке или null */
        public String       getError()       { return error; }
        /** @return каталог с байткодом */
        public Path         getClassesDir()  { return classesDir; }
        /** @return FQCN всех скомпилированных классов */
        public List<String> getAllClasses()  { return allClasses; }

        /** Удаляет временную директорию компиляции */
        public void cleanup() {
            if (tempDir != null && Files.exists(tempDir)) {
                try {
                    Files.walk(tempDir)
                         .sorted((a, b) -> b.compareTo(a))
                         .forEach(p -> {
                             try { Files.deleteIfExists(p); }
                             catch (IOException ignored) {}
                         });
                } catch (IOException ignored) {}
            }
        }
    }
}
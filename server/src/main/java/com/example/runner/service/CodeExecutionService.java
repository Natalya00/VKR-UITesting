package com.example.runner.service;

import com.example.runner.dto.RunCodeRequest;
import com.example.runner.dto.RunCodeResponse;
import com.example.runner.dto.module3.Module3ValidationRules;
import com.example.runner.model.ExerciseData;
import com.example.runner.model.ValidationRules;
import com.example.runner.service.module3.Module3ValidationPipeline;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeExecutionService {

    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(180);

    @Value("${runner.default-base-url:http://localhost:5173}")
    private String defaultBaseUrl;

    private final Module3ValidationPipeline module3Pipeline = new Module3ValidationPipeline();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final java.util.Map<Integer, ValidationRules> module2RulesCache = new java.util.HashMap<>();

    private static final Map<Integer, String> MODULE2_EXERCISE_PATHS = buildModule2ExercisePaths();

    private static Map<Integer, String> buildModule2ExercisePaths() {
        Map<Integer, String> paths = new java.util.LinkedHashMap<>();
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/exercises/module-2/**/*.json");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null && filename.startsWith("exercise-") && filename.endsWith(".json")) {
                    int order = Integer.parseInt(filename.replace("exercise-", "").replace(".json", ""));
                    paths.put(order, resource.getURL().getPath());
                }
            }
        } catch (Exception e) {
            System.err.println("[CodeExecutionService] Ошибка построения маппинга Module 2: " + e.getMessage());
        }
        return Map.copyOf(paths);
    }


    private static final Map<Integer, String> MODULE3_EXERCISE_PATHS = buildExercisePaths();

    private static Map<Integer, String> buildExercisePaths() {
        Map<Integer, String> paths = new java.util.LinkedHashMap<>();
        for (int i = 1; i <= 11; i++)
            paths.put(100 + i, "data/exercises/module-3/block-1/exercise-" + i + ".json");
        for (int i = 1; i <= 13; i++)
            paths.put(111 + i, "data/exercises/module-3/block-2/exercise-" + i + ".json");
        for (int i = 1; i <= 7; i++)
            paths.put(124 + i, "data/exercises/module-3/block-3/exercise-" + i + ".json");
        for (int i = 1; i <= 12; i++)
            paths.put(131 + i, "data/exercises/module-3/block-4/exercise-" + i + ".json");
        for (int i = 1; i <= 14; i++)
            paths.put(143 + i, "data/exercises/module-3/block-5/exercise-" + i + ".json");
        return Map.copyOf(paths);
    }

    private boolean isModule3Exercise(int exerciseNum) {
        return MODULE3_EXERCISE_PATHS.containsKey(exerciseNum);
    }


    private Object resolveStudentInput(RunCodeRequest request) {
        if (request.files() != null && !request.files().isEmpty()) {
            return request.files();
        }
        if (request.code() != null && !request.code().isBlank()) {
            return request.code();
        }
        return null;
    }


    private String resolveCodeString(RunCodeRequest request) {
        if (request.code() != null && !request.code().isBlank()) {
            return request.code();
        }
        if (request.files() != null && !request.files().isEmpty()) {
            return String.join("\n\n", request.files().values());
        }
        return null;
    }


    private boolean isUiTestCode(String code) {
        if (code == null) return false;
        String lower = code.toLowerCase();
        for (String n : new String[]{
                "import com.codeborne.selenide", "import org.openqa.selenium",
                "import com.microsoft.playwright", "selenide", "selenium", "$(", "open("}) {
            if (lower.contains(n)) return true;
        }
        return false;
    }


    private RunCodeResponse validateModule3Code(Object input, int exerciseNum, String baseUrl) {
        try {
            System.out.println("[Module3] Валидация упражнения " + exerciseNum + ", baseUrl: " + baseUrl);
            Module3ValidationRules rules = loadModule3Rules(exerciseNum);
            if (rules == null) {
                System.err.println("[Module3] Правила не загружены для упражнения " + exerciseNum);
                return new RunCodeResponse(false, true, "",
                        "Правила валидации не найдены для упражнения " + exerciseNum,
                        "❌ Ошибка конфигурации — обратитесь к преподавателю");
            }
            System.out.println("[Module3] Правила загружены успешно");

            Module3ValidationPipeline.PipelineResult result =
                    module3Pipeline.validate(input, rules, baseUrl);

            if (result.isSuccess()) {
                return new RunCodeResponse(true, true, "",
                        "✅ Все проверки пройдены успешно", 
                        "Упражнение выполнено!");
            } else {
                return new RunCodeResponse(false, true, "",
                        result.getErrorsAsString(), 
                        "❌ Код не соответствует требованиям:\n" + result.getErrorsAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new RunCodeResponse(false, true, "",
                    "Внутренняя ошибка валидации: " + e.getMessage(), 
                    "❌ Ошибка сервера: " + e.getMessage());
        }
    }

    private Module3ValidationRules loadModule3Rules(int exerciseNum) {
        String resourcePath = MODULE3_EXERCISE_PATHS.get(exerciseNum);
        if (resourcePath == null) {
            System.err.println("[Module3] Путь не найден для упражнения " + exerciseNum);
            return null;
        }
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                System.err.println("[Module3] Файл не найден: " + resourcePath);
                return null;
            }
            JsonNode exerciseJson = objectMapper.readTree(resource.getInputStream());
            JsonNode validationRulesNode = exerciseJson.get("validationRules");
            if (validationRulesNode == null || validationRulesNode.isNull()) {
                System.err.println("[Module3] validationRules не найдено в " + resourcePath);
                return null;
            }
            return objectMapper.treeToValue(validationRulesNode, Module3ValidationRules.class);
        } catch (IOException e) {
            System.err.println("[Module3] Ошибка загрузки правил для упражнения "
                    + exerciseNum + ": " + e.getMessage());
            return null;
        }
    }


    private boolean validateStudentCode(String code, List<String> requiredMethods,
                                        List<List<String>> alternativeMethods,
                                        List<String> requiredSelectors,
                                        List<List<String>> alternativeSelectors,
                                        List<String> requiredConditions,
                                        List<List<String>> alternativeConditions,
                                        String requiredFinder,
                                        StringBuilder logBuffer) {
        if (code == null || code.isBlank()) {
            log("[CodeValidator] FAIL: Code is null or empty", logBuffer);
            return false;
        }

        String codeToAnalyze = removeComments(code).lines()
                .filter(line -> !line.strip().startsWith("import "))
                .collect(Collectors.joining("\n"));

        boolean allValid = true;

        if (requiredMethods != null) {
            for (String method : requiredMethods) {
                String pat = List.of("visible", "disappear", "disabled", "exist", "text",
                        "value", "attribute", "cssClass", "checked", "selected",
                        "sortedBy", "size", "textsInAnyOrder").contains(method)
                        ? "\\b" + Pattern.quote(method) + "\\b"
                        : "\\." + Pattern.quote(method) + "\\s*\\(|(?<![.\\w])"
                                + Pattern.quote(method) + "\\s*\\(";
                if (!Pattern.compile(pat).matcher(codeToAnalyze).find()) {
                    log("[CodeValidator] FAIL: Required method '" + method + "' not found", logBuffer);
                    log("[CodeValidator] FAIL: Required method '" + method + "' not found", null);
                    allValid = false;
                } else {
                    log("[CodeValidator] OK: Found '" + method + "'", null);
                }
            }
        }

        if (alternativeMethods != null) {
            for (List<String> group : alternativeMethods) {
                boolean found = false;
                for (String method : group) {
                    String pat = List.of("visible", "disappear", "disabled", "exist", "text",
                            "value", "attribute", "cssClass", "checked", "selected",
                            "sortedBy", "size", "textsInAnyOrder").contains(method)
                            ? "\\b" + Pattern.quote(method) + "\\b"
                            : "\\." + Pattern.quote(method) + "\\s*\\(|(?<![.\\w])"
                                    + Pattern.quote(method) + "\\s*\\(";
                    if (Pattern.compile(pat).matcher(codeToAnalyze).find()) {
                        found = true;
                        log("[CodeValidator] OK: Found alternative method '" + method + "'", null);
                        break;
                    }
                }
                if (!found) {
                    log("[CodeValidator] FAIL: None of alternative methods found: " + group, logBuffer);
                    log("[CodeValidator] FAIL: None of alternative methods found: " + group, null);
                    allValid = false;
                }
            }
        }

        if (requiredConditions != null) {
            for (String cond : requiredConditions) {
                if (!Pattern.compile("\\b" + Pattern.quote(cond) + "\\b")
                        .matcher(codeToAnalyze).find()) {
                    log("[CodeValidator] FAIL: Required condition '" + cond + "' not found", logBuffer);
                    log("[CodeValidator] FAIL: Required condition '" + cond + "' not found", null);
                    allValid = false;
                } else {
                    log("[CodeValidator] OK: Found condition '" + cond + "'", null);
                }
            }
        }

        if (alternativeConditions != null) {
            for (List<String> group : alternativeConditions) {
                boolean found = false;
                for (String cond : group) {
                    if (Pattern.compile("\\b" + Pattern.quote(cond) + "\\b")
                            .matcher(codeToAnalyze).find()) {
                        found = true;
                        log("[CodeValidator] OK: Found alternative condition '" + cond + "'", null);
                        break;
                    }
                }
                if (!found) {
                    log("[CodeValidator] FAIL: None of alternative conditions found: " + group, logBuffer);
                    log("[CodeValidator] FAIL: None of alternative conditions found: " + group, null);
                    allValid = false;
                }
            }
        }

        if (requiredSelectors != null) {
            for (String sel : requiredSelectors) {
                boolean inQuotes = Pattern.compile("[\"']" + Pattern.quote(sel) + "[\"']")
                        .matcher(codeToAnalyze).find();
                boolean asSubstring = codeToAnalyze.contains(sel);
                boolean asMethod = Pattern.compile(Pattern.quote(sel) + "\\s*\\(")
                        .matcher(codeToAnalyze).find();
                boolean asMethodArg = Pattern.compile("\\w+\\s*\\([^)]*[\"']" + Pattern.quote(sel) + "[\"']")
                        .matcher(codeToAnalyze).find();
                if (!inQuotes && !asSubstring && !asMethod && !asMethodArg) {
                    log("[CodeValidator] FAIL: Required selector '" + sel + "' not found", logBuffer);
                    log("[CodeValidator] FAIL: Required selector '" + sel + "' not found", null);
                    allValid = false;
                } else {
                    log("[CodeValidator] OK: Found selector '" + sel + "'", null);
                }
            }
        }

        if (alternativeSelectors != null) {
            for (List<String> group : alternativeSelectors) {
                boolean found = false;
                for (String sel : group) {
                    boolean inQuotes = Pattern.compile("[\"']" + Pattern.quote(sel) + "[\"']")
                            .matcher(codeToAnalyze).find();
                    boolean asSubstring = codeToAnalyze.contains(sel);
                    boolean asMethod = Pattern.compile(Pattern.quote(sel) + "\\s*\\(")
                            .matcher(codeToAnalyze).find();
                    boolean asMethodArg = Pattern.compile("\\w+\\s*\\([^)]*[\"']" + Pattern.quote(sel) + "[\"']")
                            .matcher(codeToAnalyze).find();
                    if (inQuotes || asSubstring || asMethod || asMethodArg) {
                        found = true;
                        log("[CodeValidator] OK: Found alternative selector '" + sel + "'", null);
                        break;
                    }
                }
                if (!found) {
                    log("[CodeValidator] FAIL: None of alternative selectors found: " + group, logBuffer);
                    log("[CodeValidator] FAIL: None of alternative selectors found: " + group, null);
                    allValid = false;
                }
            }
        }

        if (requiredFinder != null && !requiredFinder.isBlank()) {
            if (!Pattern.compile(Pattern.quote(requiredFinder) + "\\s*\\(")
                    .matcher(codeToAnalyze).find()) {
                log("[CodeValidator] FAIL: Required finder '" + requiredFinder + "' not found", logBuffer);
                log("[CodeValidator] FAIL: Required finder '" + requiredFinder + "' not found", null);
            } else {
                log("[CodeValidator] OK: Found finder '" + requiredFinder + "'", null);
            }
        }

        if (allValid) {
            log("[CodeValidator] Статическая проверка: УСПЕХ", logBuffer);
        }

        return allValid;
    }

    private void log(String msg, StringBuilder lb) {
        if (lb != null) lb.append(msg).append("\n");
        else System.err.println(msg);
    }

    private String removeComments(String code) {
        return code.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "")
                   .replaceAll("//.*", "");
    }


    private boolean validateFromJsonRules(String code, int exerciseNum, StringBuilder logBuffer) {
        ValidationRules rules = loadModule2Rules(exerciseNum);
        if (rules == null) {
            log("[CodeValidator] Правила не найдены для упражнения " + exerciseNum + " — пропускаем валидацию", null);
            return true;
        }

        log("[CodeValidator] Выполняется статическая проверка кода...", logBuffer);
        
        return validateStudentCode(
            code,
            rules.getRequiredMethods(),
            rules.getAlternativeMethods(),
            rules.getRequiredSelectors(),
            rules.getAlternativeSelectors(),
            rules.getRequiredConditions(),
            rules.getAlternativeConditions(),
            rules.getRequiredFinder(),
            logBuffer
        );
    }


    private ValidationRules loadModule2Rules(int exerciseNum) {
        ValidationRules cached = module2RulesCache.get(exerciseNum);
        if (cached != null) {
            return cached;
        }
        
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:data/exercises/module-2/**/*.json");
            
            for (Resource resource : resources) {
                JsonNode exerciseJson = objectMapper.readTree(resource.getInputStream());
                JsonNode orderNode = exerciseJson.get("order");
                
                if (orderNode == null || !orderNode.isInt()) {
                    continue;
                }
                
                int order = orderNode.asInt();
                if (order == exerciseNum) {
                    JsonNode validationRulesNode = exerciseJson.get("validationRules");
                    
                    if (validationRulesNode == null || validationRulesNode.isNull()) {
                        log("[Module2Rules] validationRules не найдено в упражнении " + exerciseNum, null);
                        return null;
                    }
                    
                    ValidationRules rules = objectMapper.treeToValue(validationRulesNode, ValidationRules.class);
                    log("[Module2Rules] Правила загружены из файла: " + resource.getFilename(), null);
                    
                    module2RulesCache.put(exerciseNum, rules);
                    return rules;
                }
            }
            
            log("[Module2Rules] Упражнение с order=" + exerciseNum + " не найдено", null);
            return null;
        } catch (Exception e) {
            log("[Module2Rules] Ошибка загрузки правил для упражнения " + exerciseNum + ": " + e.getMessage(), null);
            return null;
        }
    }


    public RunCodeResponse runCode(RunCodeRequest request) {
        try {
            Integer exerciseNum = request.exercise();
            
            System.out.println("[runCode] exercise=" + exerciseNum + ", code.length=" + 
                (request.code() != null ? request.code().length() : "null") + 
                ", files.count=" + (request.files() != null ? request.files().size() : "null"));

            if (exerciseNum != null && isModule3Exercise(exerciseNum)) {
                System.out.println("[runCode] Обработка модуля 3 (POM)");
                Object studentInput = resolveStudentInput(request);
                if (studentInput == null) {
                    return new RunCodeResponse(false, false, "",
                            "Код или файлы не должны быть пустыми", "Введите код перед запуском");
                }
                System.out.println("[runCode] studentInput type: " + studentInput.getClass().getSimpleName());
                String baseUrl = (request.baseUrl() == null || request.baseUrl().isBlank())
                        ? defaultBaseUrl : request.baseUrl();
                return validateModule3Code(studentInput, exerciseNum, baseUrl);
            }

            System.out.println("[runCode] Обработка модуля 2 (Selenide)");
            String code = resolveCodeString(request);
            if (code == null) {
                return new RunCodeResponse(false, false, "",
                        "Код не должен быть пустым", "Введите код перед запуском");
            }

            Path workingDir = null;
            try {
                workingDir = Files.createTempDirectory("runner-" + UUID.randomUUID());
                Path sourceDir  = Files.createDirectories(workingDir.resolve("src"));
                Path classesDir = Files.createDirectories(workingDir.resolve("classes"));

                if (isUiTestCode(code)) {
                    Path userFile   = sourceDir.resolve("UserScript.java");
                    Path runnerFile = sourceDir.resolve("TestRunner.java");
                    writeUserCode(userFile, code);
                    writeRunnerCode(runnerFile);

                    String compileDiagnostics = compileSources(List.of(userFile, runnerFile), classesDir);
                    
                    if (compileDiagnostics != null) {
                        return new RunCodeResponse(false, true, "", compileDiagnostics, "Ошибка компиляции");
                    }

                    String baseUrl = (request.baseUrl() == null || request.baseUrl().isBlank())
                            ? defaultBaseUrl : request.baseUrl();
                    return executeRunner(classesDir, baseUrl, request, code);

                } else {
                    String userMainClass = writeUserSources(sourceDir, code);
                    if (userMainClass != null) {
                        String compileDiagnostics = compileSources(
                                List.of(sourceDir.resolve(userMainClass + ".java")), classesDir);
                        
                        if (compileDiagnostics != null) {
                            return new RunCodeResponse(false, false, "", compileDiagnostics, "Ошибка компиляции");
                        }
                        return executeMainClass(classesDir, userMainClass);
                    } else {
                        Path userFile = sourceDir.resolve("UserScript.java");
                        writeUserCode(userFile,
                                "public class UserScript {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        " + code + "\n" +
                                "    }\n}\n");
                        
                        String compileDiagnostics = compileSources(List.of(userFile), classesDir);
                        
                        if (compileDiagnostics != null) {
                            return new RunCodeResponse(false, false, "", compileDiagnostics, "Ошибка компиляции");
                        }
                        return executeMainClass(classesDir, "UserScript");
                    }
                }
            } catch (IOException e) {
                return new RunCodeResponse(false, false, "", e.getMessage(), "Ошибка подготовки окружения");
            } finally {
                if (workingDir != null) deleteQuietly(workingDir);
            }
        }  catch (Exception e) {  
            e.printStackTrace();
            return new RunCodeResponse(false, false, "", e.getMessage(), "Ошибка выполнения: " + e.getMessage());
        }
    }


    private void writeUserCode(Path file, String rawCode) throws IOException {
        String codeToWrite = rawCode == null ? "" : rawCode;
        String lower = codeToWrite.toLowerCase();
        boolean looksLikeFullSource = lower.contains("class ") || lower.contains("interface ")
                || lower.contains("enum ") || lower.contains("package ");

        if (!looksLikeFullSource) {
            List<String> importLines = new ArrayList<>();
            StringBuilder bodyBuilder = new StringBuilder();
            for (String l : codeToWrite.split("\\r?\\n")) {
                String t = l == null ? "" : l.strip();
                if (t.startsWith("import ")) importLines.add(t);
                else bodyBuilder.append(l).append(System.lineSeparator());
            }

            boolean hasSelenium = importLines.stream()
                    .anyMatch(imp -> imp.contains("selenide") || imp.contains("selenium"))
                    || lower.contains("selenide") || lower.contains("selenium")
                    || lower.contains("$") || lower.contains("open(");

            StringBuilder sb = new StringBuilder();
            importLines.forEach(imp -> sb.append(imp).append(System.lineSeparator()));
            if (!importLines.isEmpty()) sb.append(System.lineSeparator());
            sb.append("public class UserScript {").append(System.lineSeparator());
            sb.append(hasSelenium
                    ? "    public static void run() throws Exception {"
                    : "    public static void main(String[] args) {")
              .append(System.lineSeparator());

            for (String line : bodyBuilder.toString().split("\\r?\\n")) {
                String trimmed = line.trim();
                if (trimmed.matches(".*\\bopen\\s*\\(.*\\).*;.*")
                        && !trimmed.contains("$") && !trimmed.contains("assert")) continue;
                sb.append(line).append(System.lineSeparator());
            }
            sb.append("    }").append(System.lineSeparator())
              .append("}").append(System.lineSeparator());
            codeToWrite = sb.toString();
        }
        
        Files.writeString(file, codeToWrite, StandardCharsets.UTF_8);
    }

    private void writeRunnerCode(Path file) throws IOException {
        try (var stream = getClass().getResourceAsStream("/templates/TestRunnerTemplate.java")) {
            if (stream == null) throw new IOException("TestRunnerTemplate.java not found in classpath");
            String src = new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("package com.example.runner.service;", "")
                    .replace("TestRunnerTemplate", "TestRunner")
                    .replace("/* __INJECT_USER_SCRIPT__ */",
                            "try {\n" +
                            "        UserScript.run();\n" +
                            "        System.out.println(\"[TestRunner] UserScript executed successfully\");\n" +
                            "      } catch (Exception userEx) {\n" +
                            "        System.err.println(\"[TestRunner] UserScript error: \" + userEx.getMessage());\n" +
                            "        throw userEx;\n" +
                            "      }");
            
            Files.writeString(file, src, StandardCharsets.UTF_8);
        }
    }

    private String writeUserSources(Path sourceDir, String rawCode) throws IOException {
        if (rawCode == null) return null;
        Pattern pubClass = Pattern.compile("public\\s+class\\s+([A-Za-z_][A-Za-z0-9_]*)");
        Matcher m = pubClass.matcher(rawCode);
        if (m.find()) {
            String className = m.group(1);
            Files.writeString(sourceDir.resolve(className + ".java"), rawCode, StandardCharsets.UTF_8);
            if (Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(").matcher(rawCode).find())
                return className;
            return null;
        }
        Pattern anyClass = Pattern.compile("class\\s+([A-Za-z_][A-Za-z0-9_]*)");
        Matcher m2 = anyClass.matcher(rawCode);
        if (m2.find() && (rawCode.contains("static void main(") || rawCode.contains("public static void main("))) {
            String className = m2.group(1);
            Files.writeString(sourceDir.resolve(className + ".java"), rawCode, StandardCharsets.UTF_8);
            return className;
        }
        writeUserCode(sourceDir.resolve("UserScript.java"), rawCode);
        return null;
    }


    private String compileSources(List<Path> sources, Path outputDir) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) return "Java compiler not available. Please run on JDK.";

        try (StandardJavaFileManager fm =
                     compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {

            Iterable<? extends javax.tools.JavaFileObject> units =
                    fm.getJavaFileObjectsFromFiles(sources.stream().map(Path::toFile).toList());

            List<String> cp = new ArrayList<>();
            String rcp = System.getProperty("java.class.path");
            if (rcp != null && !rcp.isBlank()) cp.add(rcp);
            try {
                Path libDir = Path.of("/app/lib");
                if (Files.exists(libDir)) {
                    try (var s = Files.list(libDir)) {
                        s.filter(p -> p.toString().endsWith(".jar"))
                         .forEach(p -> cp.add(p.toAbsolutePath().toString()));
                    }
                }
            } catch (Exception ignored) {}

            javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diag =
                    new javax.tools.DiagnosticCollector<>();
            JavaCompiler.CompilationTask task = compiler.getTask(null, fm, diag,
                    List.of("-classpath", String.join(File.pathSeparator, cp),
                            "-d", outputDir.toAbsolutePath().toString()),
                    null, units);

            if (Boolean.TRUE.equals(task.call())) return null;

            StringBuilder sb = new StringBuilder();
            for (javax.tools.Diagnostic<? extends javax.tools.JavaFileObject> d : diag.getDiagnostics()) {
                sb.append("[").append(d.getKind()).append("] ")
                  .append(d.getSource() == null ? "Unknown" : d.getSource().getName())
                  .append(":").append(d.getLineNumber())
                  .append(" - ").append(d.getMessage(null))
                  .append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    private RunCodeResponse executeRunner(Path classesDir, String baseUrl,
                                          RunCodeRequest request, String code) {
        try {
            Integer exerciseNum = request.exercise() != null ? request.exercise() : 1;
            
            List<String> cp = new ArrayList<>();
            cp.add(classesDir.toAbsolutePath().toString());
            String rcp = System.getProperty("java.class.path");
            if (rcp != null && !rcp.isBlank()) cp.add(rcp);
            try {
                Path libDir = Path.of("/app/lib");
                if (Files.exists(libDir)) {
                    try (var s = Files.list(libDir)) {
                        s.filter(p -> p.toString().endsWith(".jar"))
                         .forEach(p -> cp.add(p.toAbsolutePath().toString()));
                    }
                }
            } catch (Exception ignored) {}

            Path loggingFile = classesDir.getParent().resolve("logging.properties");
            Files.writeString(loggingFile,
                    "org.openqa.selenium.devtools.CdpVersionFinder.level=SEVERE\n" +
                    "org.openqa.selenium.chromium.ChromiumDriver.level=SEVERE\n" +
                    "org.openqa.selenium.level=SEVERE\n", StandardCharsets.UTF_8);

            ProcessBuilder builder = new ProcessBuilder("java",
                    "-Djava.util.logging.config.file=" + loggingFile.toAbsolutePath(),
                    "-cp", String.join(File.pathSeparator, cp),
                    "TestRunner", baseUrl,
                    String.valueOf(exerciseNum));
            builder.redirectErrorStream(false);

            Process process = builder.start();
            ProcessOutputCollector stdout = new ProcessOutputCollector(process.getInputStream(), true);
            ProcessOutputCollector stderr = new ProcessOutputCollector(process.getErrorStream(), true);
            stdout.start(); stderr.start();

            boolean finished = process.waitFor(
                    PROCESS_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return new RunCodeResponse(false, true,
                        stdout.getOutput(), stderr.getOutput(), 
                        "⏱️ Время выполнения истекло (" + PROCESS_TIMEOUT.getSeconds() + " сек)\n\nУбедитесь, что ваш код не содержит бесконечных циклов и ожиданий.");
            }

            boolean dynamicSuccess = process.exitValue() == 0;
            Integer exNum = request.exercise();
            StringBuilder validationLog = new StringBuilder();
            boolean staticSuccess = true;

            if (exNum != null && exNum >= 1 && exNum <= 90) {
                staticSuccess = validateFromJsonRules(code, exNum, validationLog);
            }

            boolean success = dynamicSuccess && staticSuccess;

            String stderrOut = stderr.getOutput();
            if (!validationLog.isEmpty()) stderrOut = validationLog + "\n" + stderrOut;

            String message;
            if (success) {
                message = "✅ Динамическая проверка: УСПЕХ\n✅ Статическая проверка: УСПЕХ\n\n🎉 Упражнение выполнено!";
            } else if (!dynamicSuccess && staticSuccess) {
                message = "❌ Динамическая проверка: НЕ ПРОЙДЕНА\n✅ Статическая проверка: УСПЕХ\n\nТест не прошёл в браузере. Проверьте локаторы и действия.";
            } else if (dynamicSuccess && !staticSuccess) {
                message = "✅ Динамическая проверка: УСПЕХ\n❌ Статическая проверка: НЕ ПРОЙДЕНА\n\nКод не соответствует требованиям упражнения. Проверьте методы, селекторы и условия.";
            } else {
                message = "❌ Динамическая проверка: НЕ ПРОЙДЕНА\n❌ Статическая проверка: НЕ ПРОЙДЕНА\n\nОбе проверки не пройдены. Исправьте код.";
            }

            return new RunCodeResponse(success, true, stdout.getOutput(), stderrOut, message);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RunCodeResponse(false, true, "", e.getMessage(), "Ошибка выполнения теста");
        }
    }

    private RunCodeResponse executeMainClass(Path classesDir, String mainClass) {
        try {
            Path loggingFile = classesDir.getParent().resolve("logging.properties");
            Files.writeString(loggingFile, "org.openqa.selenium.level=SEVERE\n",
                    StandardCharsets.UTF_8);

            ProcessBuilder builder = new ProcessBuilder("java",
                    "-Djava.util.logging.config.file=" + loggingFile.toAbsolutePath(),
                    "-cp", classesDir.toAbsolutePath().toString(), mainClass);
            builder.redirectErrorStream(false);

            Process process = builder.start();
            ProcessOutputCollector stdout = new ProcessOutputCollector(process.getInputStream());
            ProcessOutputCollector stderr = new ProcessOutputCollector(process.getErrorStream());
            stdout.start(); stderr.start();

            boolean finished = process.waitFor(
                    PROCESS_TIMEOUT.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new RunCodeResponse(false, false,
                        stdout.getOutput(), stderr.getOutput(), "Время выполнения истекло");
            }

            boolean success = process.exitValue() == 0;
            return new RunCodeResponse(success, false, stdout.getOutput(), stderr.getOutput(),
                    success ? "Выполнение завершено" : "Программа завершилась с кодом ошибки");

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new RunCodeResponse(false, false, "", e.getMessage(), "Ошибка выполнения программы");
        }
    }


    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.walk(path).sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private static class ProcessOutputCollector extends Thread {
        private final java.io.InputStream inputStream;
        private final StringBuilder output = new StringBuilder();
        private final boolean filter;

        ProcessOutputCollector(java.io.InputStream is, boolean filter) {
            this.inputStream = is; this.filter = filter;
        }
        ProcessOutputCollector(java.io.InputStream is) { this(is, false); }

        @Override
        public void run() {
            try (java.io.BufferedReader r = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (filter && shouldFilter(line)) continue;
                    output.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignored) {}
        }

        private boolean shouldFilter(String line) {
            if (line == null || line.isEmpty()) return false;
            String l = line.toLowerCase();
            return l.contains("org.openqa.selenium") || l.contains("org.seleniumhq")
                    || l.contains("devtools") || l.contains("chromiumdriver")
                    || l.contains("cdpversionfinder") || l.contains("webdrivermanager")
                    || (l.contains("selenide:") && !l.contains("error")
                            && !l.contains("exception") && !l.contains("fail"))
                    || (l.contains("starting") && l.contains("selenium"))
                    || (l.contains("configuring") && l.contains("browser"));
        }

        String getOutput() {
            if (filter) return output.toString().lines()
                    .filter(l -> !shouldFilter(l)).collect(Collectors.joining("\n"));
            return output.toString().trim();
        }
    }
}
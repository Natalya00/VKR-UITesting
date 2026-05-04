package com.example.runner.service.module3.ast;

import com.example.runner.dto.RunCodeRequest;
import com.example.runner.dto.RunCodeResponse;
import com.example.runner.service.CodeExecutionService;
import com.example.runner.service.module3.Module3ValidationPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CodeExecutionService Tests")
class CodeExecutionServiceTest {

    @Mock
    private Module3ValidationPipeline module3Pipeline;

    @InjectMocks
    private CodeExecutionService codeExecutionService;

    private final String defaultBaseUrl = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(codeExecutionService, "defaultBaseUrl", defaultBaseUrl);
    }

    @Test
    @DisplayName("Должен обрабатывать пустой код")
    void shouldHandleEmptyCode() {
        RunCodeRequest request = new RunCodeRequest(null, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertFalse(response.success());
        assertFalse(response.uiMode());
        assertEquals("Код не должен быть пустым", response.stderr());
        assertEquals("Введите код перед запуском", response.message());
    }

    @Test
    @DisplayName("Должен обрабатывать пустые файлы для модуля 3")
    void shouldHandleEmptyFilesForModule3() {
        RunCodeRequest request = new RunCodeRequest(null, null, 101, "module-3", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertFalse(response.success());
        assertFalse(response.uiMode());
        assertEquals("Код или файлы не должны быть пустыми", response.stderr());
        assertEquals("Введите код перед запуском", response.message());
    }

    @Test
    @DisplayName("Должен определять UI тестовый код")
    void shouldIdentifyUiTestCode() {
        String selenideCode = "import static com.codeborne.selenide.Selenide.*; $(\".button\").click();";
        RunCodeRequest request = new RunCodeRequest(selenideCode, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Должен определять обычный Java код")
    void shouldIdentifyRegularJavaCode() {
        String javaCode = "System.out.println(\"Hello World\");";
        RunCodeRequest request = new RunCodeRequest(javaCode, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Должен использовать переданный базовый URL")
    void shouldUseProvidedBaseUrl() {
        String customBaseUrl = "http://custom-url:3000";
        String selenideCode = "import static com.codeborne.selenide.Selenide.*; open(\"/\");";
        RunCodeRequest request = new RunCodeRequest(selenideCode, customBaseUrl, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Должен обрабатывать код с файлами для модуля 2")
    void shouldHandleCodeWithFilesForModule2() {
        Map<String, String> files = Map.of(
            "TestClass.java", "public class TestClass { public static void main(String[] args) { System.out.println(\"Test\"); } }"
        );
        RunCodeRequest request = new RunCodeRequest(null, null, 1, "module-2", "test-exercise", files);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Должен обрабатывать различные типы упражнений")
    void shouldHandleDifferentExerciseTypes() {
        String code = "System.out.println(\"Test\");";
        int[] exerciseNumbers = {1, 15, 45, 89};

        for (int exerciseNum : exerciseNumbers) {
            RunCodeRequest request = new RunCodeRequest(code, null, exerciseNum, "module-2", "test-exercise", null);
            RunCodeResponse response = codeExecutionService.runCode(request);
            assertNotNull(response, "Response should not be null for exercise " + exerciseNum);
        }
    }

    @Test
    @DisplayName("Должен обрабатывать Selenide код с различными методами")
    void shouldHandleSelenideCodeWithVariousMethods() {
        String selenideCode = """
            import static com.codeborne.selenide.Selenide.*;
            import static com.codeborne.selenide.Condition.*;
            open("/");
            $(".button").click();
            $(".input").setValue("test");
            $(".result").shouldBe(visible);
            """;
        RunCodeRequest request = new RunCodeRequest(selenideCode, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertTrue(response.uiMode());
    }

    @Test
    @DisplayName("Должен обрабатывать различные Selenide селекторы")
    void shouldHandleVariousSelenideSelectors() {
        String selenideWithSelectors = """
            import static com.codeborne.selenide.Selenide.*;
            $("#id-selector").click();
            $(".class-selector").setValue("test");
            $(byText("Click me")).click();
            """;
        RunCodeRequest request = new RunCodeRequest(selenideWithSelectors, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertTrue(response.uiMode());
    }

    @Test
    @DisplayName("Должен обрабатывать null значения в запросе")
    void shouldHandleNullValuesInRequest() {
        RunCodeRequest request = new RunCodeRequest(null, null, null, null, null, null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertFalse(response.success());
    }

    @Test
    @DisplayName("Должен обрабатывать пустые строки")
    void shouldHandleEmptyStrings() {
        RunCodeRequest request = new RunCodeRequest("", null, 1, "module-2", "", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertFalse(response.success());
        assertEquals("Код не должен быть пустым", response.stderr());
    }

    @Test
    @DisplayName("Должен обрабатывать код только с пробелами")
    void shouldHandleWhitespaceOnlyCode() {
        RunCodeRequest request = new RunCodeRequest("   \n\t  \r\n  ", null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertFalse(response.success());
        assertEquals("Код не должен быть пустым", response.stderr());
    }

    @Test
    @DisplayName("Должен обрабатывать смешанный Selenide код")
    void shouldHandleMixedSelenideCode() {
        String mixedCode = """
            import static com.codeborne.selenide.Selenide.*;
            import java.util.List;
            open("/");
            $(".input").setValue("test");
            $(".submit").click();
            """;
        RunCodeRequest request = new RunCodeRequest(mixedCode, null, 1, "module-2", "test-exercise", null);

        RunCodeResponse response = codeExecutionService.runCode(request);

        assertNotNull(response);
        assertTrue(response.uiMode());
    }
}
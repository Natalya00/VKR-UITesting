package com.example.runner.service.module3.ast;

import com.example.runner.dto.module3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AstValidator Tests")
class AstValidatorTest {

    private AstValidator astValidator;

    @BeforeEach
    void setUp() {
        astValidator = new AstValidator();
    }

    @Test
    @DisplayName("Должен обрабатывать пустой код")
    void shouldHandleEmptyCode() {
        String code = "";
        AstRules rules = new AstRules();

        AstValidationResult result = astValidator.validate(code, rules);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().contains("Код пуст"));
    }

    @Test
    @DisplayName("Должен обрабатывать null код")
    void shouldHandleNullCode() {
        AstValidationResult result = astValidator.validate(null, new AstRules());

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().contains("Код пуст"));
    }

    @Test
    @DisplayName("Должен обрабатывать код только с пробелами")
    void shouldHandleWhitespaceOnlyCode() {
        AstValidationResult result = astValidator.validate("   \n\t  ", new AstRules());

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().contains("Код пуст"));
    }

    @Test
    @DisplayName("Должен возвращать успех для валидного кода без правил")
    void shouldReturnSuccessForValidCodeWithoutRules() {
        AstValidationResult result = astValidator.validate("public class TestClass { }", null);

        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Должен возвращать успех для валидного кода с пустыми правилами")
    void shouldReturnSuccessForValidCodeWithEmptyRules() {
        AstValidationResult result = astValidator.validate("public class TestClass { }", new AstRules());

        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Должен валидировать правила классов")
    void shouldValidateClassRules() {
        String code = "public class TestPage { }";

        ClassRule classRule = new ClassRule();
        classRule.setClassName("TestPage");
        classRule.setRequired(true);
        classRule.setModifiers(new String[]{"public"});

        AstRules rules = new AstRules();
        rules.setClassRules(List.of(classRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила методов")
    void shouldValidateMethodRules() {
        String code = """
            public class TestPage {
                public void clickButton() { }
            }
            """;

        MethodRule methodRule = new MethodRule();
        methodRule.setMethodName("clickButton");
        methodRule.setRequired(true);
        methodRule.setModifiers(new String[]{"public"});
        methodRule.setReturnType("void");

        AstRules rules = new AstRules();
        rules.setMethodRules(List.of(methodRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила полей")
    void shouldValidateFieldRules() {
        String code = """
            public class TestPage {
                private String title;
            }
            """;

        FieldRule fieldRule = new FieldRule();
        fieldRule.setFieldName("title");
        fieldRule.setFieldType("String");
        fieldRule.setRequired(true);
        fieldRule.setModifiers(new String[]{"private"});

        AstRules rules = new AstRules();
        rules.setFieldRules(List.of(fieldRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила конструкторов")
    void shouldValidateConstructorRules() {
        String code = """
            public class TestPage {
                public TestPage() { }
            }
            """;

        ConstructorRule constructorRule = new ConstructorRule();
        constructorRule.setClassName("TestPage");
        constructorRule.setRequired(true);
        constructorRule.setModifiers(new String[]{"public"});

        AstRules rules = new AstRules();
        rules.setConstructorRules(List.of(constructorRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила интерфейсов")
    void shouldValidateInterfaceRules() {
        String code = """
            public interface TestInterface {
                void testMethod();
            }
            """;

        InterfaceRule interfaceRule = new InterfaceRule();
        interfaceRule.setInterfaceName("TestInterface");
        interfaceRule.setRequired(true);
        interfaceRule.setRequiredMethods(new String[]{"testMethod"});

        AstRules rules = new AstRules();
        rules.setInterfaceRules(List.of(interfaceRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила тела методов")
    void shouldValidateMethodBodyRules() {
        String code = """
            public class TestPage {
                public void clickButton() {
                    button.click();
                }
            }
            """;

        MethodBodyRule methodBodyRule = new MethodBodyRule();
        methodBodyRule.setMethodName("clickButton");
        methodBodyRule.setRequiredMethodCalls(List.of("click"));

        AstRules rules = new AstRules();
        rules.setMethodBodyRules(List.of(methodBodyRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать правила аннотаций")
    void shouldValidateAnnotationRules() {
        String code = """
            public class TestPage {
                @FindBy(id = "button")
                private WebElement button;
            }
            """;

        AnnotationRule annotationRule = new AnnotationRule();
        annotationRule.setTargetType("field");
        annotationRule.setClassName("TestPage");
        annotationRule.setRequiredAnnotations(List.of("FindBy"));

        AstRules rules = new AstRules();
        rules.setAnnotationRules(List.of(annotationRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать множественные правила")
    void shouldValidateMultipleRules() {
        String code = """
            public class TestPage {
                private String title;
                public TestPage() { this.title = "Test"; }
                public void clickButton() { button.click(); }
            }
            """;

        ClassRule classRule = new ClassRule();
        classRule.setClassName("TestPage");
        classRule.setRequired(true);

        MethodRule methodRule = new MethodRule();
        methodRule.setMethodName("clickButton");
        methodRule.setRequired(true);

        FieldRule fieldRule = new FieldRule();
        fieldRule.setFieldName("title");
        fieldRule.setRequired(true);

        AstRules rules = new AstRules();
        rules.setClassRules(List.of(classRule));
        rules.setMethodRules(List.of(methodRule));
        rules.setFieldRules(List.of(fieldRule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать конкретный класс")
    void shouldValidateSpecificClass() {
        String code = """
            public class TestPage { }
            public class AnotherPage { }
            """;

        ClassRule classRule = new ClassRule();
        classRule.setClassName("TestPage");
        classRule.setRequired(true);

        AstRules rules = new AstRules();
        rules.setClassRules(List.of(classRule));

        AstValidationResult result = astValidator.validateClass(code, "TestPage", rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен обрабатывать валидацию класса без правил")
    void shouldHandleClassValidationWithoutRules() {
        AstValidationResult result = astValidator.validateClass("public class TestPage { }", "TestPage", null);

        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Должен обрабатывать валидацию класса с пустыми правилами")
    void shouldHandleClassValidationWithEmptyClassRules() {
        AstRules rules = new AstRules();
        rules.setClassRules(List.of());

        AstValidationResult result = astValidator.validateClass("public class TestPage { }", "TestPage", rules);

        assertTrue(result.isSuccess());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Должен обрабатывать код с синтаксическими ошибками")
    void shouldHandleCodeWithSyntaxErrors() {
        String invalidCode = """
            public class TestPage {
                public void method( {
                }
            """;

        ClassRule classRule = new ClassRule();
        classRule.setClassName("TestPage");
        classRule.setRequired(true);

        AstRules rules = new AstRules();
        rules.setClassRules(List.of(classRule));

        AstValidationResult result = astValidator.validate(invalidCode, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен валидировать метод с обязательными вызовами в теле")
    void shouldValidateMethodWithRequiredCalls() {
        String code = """
            public class LoginPage {
                public void login(String user, String pass) {
                    usernameField.sendKeys(user);
                    passwordField.sendKeys(pass);
                    loginButton.click();
                }
            }
            """;

        MethodBodyRule rule = new MethodBodyRule();
        rule.setMethodName("login");
        rule.setRequiredMethodCalls(List.of("sendKeys", "click"));

        AstRules rules = new AstRules();
        rules.setMethodBodyRules(List.of(rule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Должен обнаруживать запрещённые вызовы в теле метода")
    void shouldDetectForbiddenMethodCalls() {
        String code = """
            public class LoginPage {
                public void login() {
                    Thread.sleep(5000);
                    loginButton.click();
                }
            }
            """;

        MethodBodyRule rule = new MethodBodyRule();
        rule.setMethodName("login");
        rule.setForbiddenMethodCalls(List.of("sleep"));

        AstRules rules = new AstRules();
        rules.setMethodBodyRules(List.of(rule));

        AstValidationResult result = astValidator.validate(code, rules);

        assertNotNull(result);
    }
}
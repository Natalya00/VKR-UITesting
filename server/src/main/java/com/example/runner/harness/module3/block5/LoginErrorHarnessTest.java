package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест сообщения об ошибке входа */
public class LoginErrorHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    @Test
    void testWrongPasswordShowsError() throws Exception {
        $("#password-input").setValue("wrongpassword");
        $("#submit-btn").click();

        $("#error-message").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testGetErrorMessage() throws Exception {
        $("#password-input").setValue("wrongpassword");
        $("#submit-btn").click();
        $("#error-message").shouldBe(visible, java.time.Duration.ofSeconds(3));

        Class<?> loginPageClass = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        Object loginPage = loginPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(loginPage, "getErrorMessage");
        assertNotNull(result, "getErrorMessage() не должен возвращать null");
        assertInstanceOf(String.class, result,
                "getErrorMessage() должен возвращать String");
        assertTrue(((String) result).length() > 0,
                "getErrorMessage() должен вернуть непустой текст");
    }

    @Test
    void testLoginWithWrongPasswordUsesAssert() throws Exception {
        Class<?> loginPageClass = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        java.lang.reflect.Method method = loginPageClass.getDeclaredMethod("getErrorMessage");
        assertNotNull(method);
        assertEquals(String.class, method.getReturnType(),
                "getErrorMessage() должен возвращать String");
    }

    @Test
    void testStudentLoginWithWrongPassword() throws Exception {
        Class<?> testClass = loadStudentClass("com.example.runner.tests.LoginTest");
        Object testInstance = testClass.getDeclaredConstructor().newInstance();

        final java.lang.reflect.Method testMethod = findMethod(testClass, "loginWithWrongPassword");
        assertNotNull(testMethod, "LoginTest должен содержать метод loginWithWrongPassword()");

        testMethod.setAccessible(true);
        assertDoesNotThrow(() -> testMethod.invoke(testInstance),
                "loginWithWrongPassword() должен выполниться без исключений");
    }

    private java.lang.reflect.Method findMethod(Class<?> cls, String name) {
        for (var m : cls.getDeclaredMethods()) {
            if (m.getName().equals(name)) return m;
        }
        return null;
    }
}

package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTestHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    private Object createAuthService() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.services.AuthService");
        return cls.getDeclaredConstructor().newInstance();
    }

    private Object createUser(String login, String password) throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.tests.User");
        return cls.getDeclaredConstructor(String.class, String.class)
                .newInstance(login, password);
    }

    @Test
    void testLoginReturnsHomePage() throws Exception {
        Object authService = createAuthService();
        Object user = createUser("testuser", "testpass");
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");

        Object result = invoke(authService, "login", user);

        assertNotNull(result, "login() должен возвращать не null");
        assertTrue(homePageClass.isInstance(result),
                "login() должен возвращать объект HomePage");
    }

    @Test
    void testLoginFillsForm() throws Exception {
        Object authService = createAuthService();
        Object user = createUser("testuser", "testpass");

        invoke(authService, "login", user);

        String loginValue = $("#login-input").getAttribute("value");
        String passwordValue = $("#password-input").getAttribute("value");

        assertEquals("testuser", loginValue,
                "login() должен заполнить поле логина");
        assertEquals("password123", passwordValue,
                "login() должен заполнить поле пароля");
    }

    @Test
    void testCheckPageLoaded() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");

        java.lang.reflect.Method method = null;
        try {
            method = homePageClass.getDeclaredMethod("checkPageLoaded");
        } catch (NoSuchMethodException e) {
            fail("HomePage должен содержать метод checkPageLoaded()");
        }

        assertNotNull(method, "Метод checkPageLoaded() должен существовать");
        assertEquals(boolean.class, method.getReturnType(),
                "checkPageLoaded() должен возвращать boolean");
    }

    @Test
    void testCheckPageLoadedRealBehavior() throws Exception {
        open("/test-harness/module3/home");

        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "checkPageLoaded");
        assertTrue((Boolean) result, "checkPageLoaded() должен вернуть true на загруженной странице");
    }
}

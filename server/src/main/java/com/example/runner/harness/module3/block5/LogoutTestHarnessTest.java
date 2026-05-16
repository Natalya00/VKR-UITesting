package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест сценария выхода */
public class LogoutTestHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
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

    private Object login() throws Exception {
        open("/test-harness/module3/login");
        $("#login-input").shouldBe(visible, java.time.Duration.ofSeconds(3));

        Object authService = createAuthService();
        Object user = createUser("testuser", "testpass");
        return invoke(authService, "login", user);
    }

    @Test
    void testLogoutReturnsLoginPage() throws Exception {
        Object homePage = login();
        Class<?> loginPageClass = loadStudentClass("com.example.runner.pom.pages.LoginPage");

        Object result = invoke(homePage, "logout");

        assertNotNull(result, "logout() не должен возвращать null");
        assertTrue(loginPageClass.isInstance(result),
                "logout() должен возвращать объект LoginPage, а не "
                        + result.getClass().getSimpleName());

        $("#login-page").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testLogoutButtonExists() throws Exception {
        $("[data-testid='logout-btn']").shouldBe(visible);
    }

}

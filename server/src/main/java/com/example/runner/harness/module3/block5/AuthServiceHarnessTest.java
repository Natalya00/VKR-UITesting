package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceHarnessTest extends AbstractHarnessTest {

    private static final String VALID_LOGIN    = "testuser";
    private static final String VALID_PASSWORD = "testpass";
    private static final String WRONG_PASSWORD = "wrongpass";

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    private Object createUser(String login, String password) throws Exception {
        Class<?> userClass = loadStudentClass("com.example.runner.tests.User");

        try {
            Constructor<?> ctor = userClass.getDeclaredConstructor(String.class, String.class);
            ctor.setAccessible(true);
            return ctor.newInstance(login, password);
        } catch (NoSuchMethodException e) {
            Object user = userClass.getDeclaredConstructor().newInstance();
            invoke(user, "setLogin", login);
            invoke(user, "setPassword", password);
            return user;
        }
    }

    private Object createAuthService() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.services.AuthService");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testLoginReturnsHomePage() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object authService = createAuthService();
        Object user = createUser(VALID_LOGIN, VALID_PASSWORD);

        Object result = invoke(authService, "login", user);

        assertNotNull(result, "login() не должен возвращать null");
        assertTrue(homePageClass.isInstance(result),
                "login() должен возвращать объект HomePage, а не "
                        + result.getClass().getSimpleName());
    }

    @Test
    void testLoginFillsForm() throws Exception {
        Object authService = createAuthService();
        Object user = createUser(VALID_LOGIN, VALID_PASSWORD);

        invoke(authService, "login", user);

        $("[data-testid='home-header']").shouldBe(visible);
    }

    @Test
    void testLoginUsesPageObject() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.services.AuthService");
        assertNotNull(cls.getMethod("login",
                loadStudentClass("com.example.runner.tests.User")),
                "Метод login(User) должен существовать в AuthService");
    }

    @Test
    void testLoginSequence() throws Exception {
        Object authService = createAuthService();
        Object user = createUser(VALID_LOGIN, VALID_PASSWORD);

        invoke(authService, "login", user);

        String userName = $("[data-testid='user-name']").getText();
        assertFalse(userName.isBlank(),
                "После успешного входа имя пользователя должно отображаться на странице");
    }
}

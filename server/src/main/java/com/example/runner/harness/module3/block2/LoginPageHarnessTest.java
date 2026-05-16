package com.example.runner.harness.module3.block2;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты страницы LoginPage */
public class LoginPageHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    private Object createLoginPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testFillLogin() throws Exception {
        Object page = createLoginPage();
        invoke(page, "fillLogin", "testuser");
        assertValue("#login-input", "testuser");
    }

    @Test
    void testFillPassword() throws Exception {
        Object page = createLoginPage();
        invoke(page, "fillPassword", "secret123");
        assertValue("#password-input", "secret123");
    }

    @Test
    void testIsLoginButtonEnabled() throws Exception {
        Object page = createLoginPage();
        Object result = invoke(page, "isLoginButtonEnabled");
        assertNotNull(result);
        assertInstanceOf(Boolean.class, result,
                "isLoginButtonEnabled() должен возвращать boolean");
    }

    @Test
    void testIsModalVisibleClosed() throws Exception {
        Object page = createLoginPage();
        Object result = invoke(page, "isModalVisible");

        assertNotNull(result);
        assertInstanceOf(Boolean.class, result,
                "isModalVisible() должен возвращать boolean");
        assertFalse((Boolean) result,
                "isModalVisible() должен вернуть false когда модалка закрыта");
    }

    @Test
    void testClickLoginButtonReturnsHomePage() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object page = createLoginPage();

        Object result = invoke(page, "clickLoginButton");

        assertNotNull(result);
        assertTrue(homePageClass.isInstance(result),
                "clickLoginButton() должен возвращать объект HomePage");
    }

    @Test
    void testGoToProfileReturnsProfilePage() throws Exception {
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");

        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "goToProfile");

        assertNotNull(result);
        assertTrue(profilePageClass.isInstance(result),
                "goToProfile() должен возвращать объект ProfilePage");
    }
}

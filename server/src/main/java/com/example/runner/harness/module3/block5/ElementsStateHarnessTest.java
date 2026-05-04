package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

public class ElementsStateHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    @Test
    void testIsLoginButtonEnabledReturnsBoolean() throws Exception {
        Class<?> loginPageClass = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        Object loginPage = loginPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(loginPage, "isLoginButtonEnabled");
        assertNotNull(result, "isLoginButtonEnabled() не должен возвращать null");
        assertInstanceOf(Boolean.class, result, "isLoginButtonEnabled() должен возвращать boolean");
    }

    @Test
    void testIsSearchBoxVisibleReturnsBoolean() throws Exception {
        open("/test-harness/module3/products");

        Class<?> productPageClass = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        Object productPage = productPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(productPage, "isSearchBoxVisible");
        assertNotNull(result, "isSearchBoxVisible() не должен возвращать null");
        assertInstanceOf(Boolean.class, result, "isSearchBoxVisible() должен возвращать boolean");
    }

    @Test
    void testEnabledButtonExists() throws Exception {
        $("#submit-btn").shouldBe(visible);
    }

    @Test
    void testSearchBoxExists() throws Exception {
        open("/test-harness/module3/products");
        $("#search-box").shouldBe(visible);
    }

    @Test
    void testIsLoginButtonEnabledRealBehavior() throws Exception {
        Class<?> loginPageClass = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        Object loginPage = loginPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(loginPage, "isLoginButtonEnabled");
        assertTrue((Boolean) result, "isLoginButtonEnabled() должен вернуть true для видимой кнопки");
    }

    @Test
    void testIsSearchBoxVisibleRealBehavior() throws Exception {
        open("/test-harness/module3/products");

        Class<?> productPageClass = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        Object productPage = productPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(productPage, "isSearchBoxVisible");
        assertTrue((Boolean) result, "isSearchBoxVisible() должен вернуть true для видимого поля");
    }
}

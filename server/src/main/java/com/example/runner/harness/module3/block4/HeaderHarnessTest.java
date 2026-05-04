package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

public class HeaderHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/login";
    }

    private Object createHeader() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Header");
        SelenideElement baseElement = $("header");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                loadStudentClass("com.codeborne.selenide.SelenideElement")
        );
        ctor.setAccessible(true);
        return ctor.newInstance(baseElement);
    }

    private Object createLoginPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.LoginPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testIsLogoVisibleReturnsTrue() throws Exception {
        Object header = createHeader();
        Object result = invoke(header, "isLogoVisible");

        assertNotNull(result, "isLogoVisible() должен возвращать не null");
        assertInstanceOf(Boolean.class, result,
                "isLogoVisible() должен возвращать boolean");
        assertTrue((Boolean) result,
                "isLogoVisible() должен вернуть true когда логотип есть на странице");
    }

    @Test
    void testIsLogoDisplayedDelegatesToHeader() throws Exception {
        Object loginPage = createLoginPage();
        Object result = invoke(loginPage, "isLogoDisplayed");

        assertNotNull(result, "isLogoDisplayed() должен возвращать не null");
        assertInstanceOf(Boolean.class, result,
                "isLogoDisplayed() должен возвращать boolean");
        assertTrue((Boolean) result,
                "isLogoDisplayed() должен вернуть true когда логотип есть на странице");
    }

    @Test
    void testHeaderHasLogoField() throws Exception {
        Class<?> headerClass = loadStudentClass("com.example.runner.pom.components.Header");

        java.lang.reflect.Field logoField = null;
        try {
            logoField = headerClass.getDeclaredField("logo");
        } catch (NoSuchFieldException e) {
            fail("Header должен содержать поле logo");
        }

        assertNotNull(logoField);
        assertEquals(
                loadStudentClass("com.codeborne.selenide.SelenideElement"),
                logoField.getType(),
                "Поле logo должно быть типа SelenideElement");
    }
}

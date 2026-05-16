package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты блока компонентов */
public class ComponentsBlockHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    private Object createHeader() throws Exception {
        Class<?> headerClass = loadStudentClass("com.example.runner.pom.components.Header");
        Constructor<?> ctor = headerClass.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        ctor.setAccessible(true);
        return ctor.newInstance($x("//header"));
    }

    private Object createFooter() throws Exception {
        Class<?> footerClass = loadStudentClass("com.example.runner.pom.components.Footer");
        Constructor<?> ctor = footerClass.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        ctor.setAccessible(true);
        return ctor.newInstance($x("//footer"));
    }

    @Test
    void testClickLogo() throws Exception {
        Object header = createHeader();

        Object result = invoke(header, "clickLogo");

        assertNotNull(result, "clickLogo() не должен возвращать null");
        assertSame(header, result, "clickLogo() должен возвращать this");
    }

    @Test
    void testClickProfile() throws Exception {
        Object header = createHeader();

        Object result = invoke(header, "clickProfile");

        assertNotNull(result, "clickProfile() не должен возвращать null");
        assertSame(header, result, "clickProfile() должен возвращать this");
    }

    @Test
    void testClickSocialLink() throws Exception {
        Object footer = createFooter();

        Object result = invoke(footer, "clickSocialLink", "vk");

        assertNotNull(result, "clickSocialLink() не должен возвращать null");
        assertSame(footer, result, "clickSocialLink() должен возвращать this");
    }

    @Test
    void testBasePageHasFields() throws Exception {
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");
        Class<?> headerClass = loadStudentClass("com.example.runner.pom.components.Header");
        Class<?> footerClass = loadStudentClass("com.example.runner.pom.components.Footer");

        var headerField = basePageClass.getDeclaredField("header");
        assertNotNull(headerField, "Поле header не найдено");
        assertEquals(headerClass, headerField.getType(), "header должен быть типа Header");

        var footerField = basePageClass.getDeclaredField("footer");
        assertNotNull(footerField, "Поле footer не найдено");
        assertEquals(footerClass, footerField.getType(), "footer должен быть типа Footer");
    }
}

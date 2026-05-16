package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.exist;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты виджета дашборда */
public class DashboardWidgetHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createToolbar() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Toolbar");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#toolbar"));
    }

    private Object createDashboardWidget() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.DashboardWidget");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#dashboard-widget"));
    }

    @Test
    void testClickRefresh() throws Exception {
        Object toolbar = createToolbar();

        invoke(toolbar, "clickRefresh");

        $("#refresh-status").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testGetToolbarReturnsToolbar() throws Exception {
        Object widget = createDashboardWidget();

        Object result = invoke(widget, "getToolbar");
        assertNotNull(result, "getToolbar() не должен возвращать null");

        Class<?> toolbarClass = loadStudentClass("com.example.runner.pom.components.Toolbar");
        assertTrue(toolbarClass.isInstance(result),
                "getToolbar() должен возвращать объект Toolbar");
    }

    @Test
    void testRefreshData() throws Exception {
        Class<?> pageClass = loadStudentClass("com.example.runner.pom.pages.DashboardPage");
        Constructor<?> pageCtor = pageClass.getDeclaredConstructor();
        Object page = pageCtor.newInstance();

        invoke(page, "refreshData");

        $("#refresh-status").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testRefreshDataReturnsThis() throws Exception {
        Class<?> pageClass = loadStudentClass("com.example.runner.pom.pages.DashboardPage");
        Constructor<?> pageCtor = pageClass.getDeclaredConstructor();
        Object page = pageCtor.newInstance();

        Object result = invoke(page, "refreshData");
        assertSame(page, result, "refreshData() должен возвращать this");
    }
}

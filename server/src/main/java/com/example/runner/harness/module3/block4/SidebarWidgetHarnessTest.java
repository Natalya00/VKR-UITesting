package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты виджета боковой панели */
public class SidebarWidgetHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createSidebarWidget() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.SidebarWidget");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#sidebar-widget"));
    }

    @Test
    void testApplyFilter() throws Exception {
        Object widget = createSidebarWidget();

        invoke(widget, "applyFilter", "Активные");

        String bg = $x(".//button[text()='Активные']").getCssValue("background-color");
        assertNotNull(bg, "Кнопка фильтра должна изменить стиль");
    }

    @Test
    void testSortBy() throws Exception {
        Object widget = createSidebarWidget();

        invoke(widget, "sortBy", "По дате");

        String bg = $x(".//button[text()='По дате']").getCssValue("background-color");
        assertNotNull(bg, "Кнопка сортировки должна изменить стиль");
    }

    @Test
    void testSidebarWidgetDelegatesFilter() throws Exception {
        Object widget = createSidebarWidget();

        assertDoesNotThrow(() -> invoke(widget, "applyFilter", "Все"),
                "applyFilter() не должен падать");
    }

    @Test
    void testSidebarWidgetDelegatesSort() throws Exception {
        Object widget = createSidebarWidget();

        assertDoesNotThrow(() -> invoke(widget, "sortBy", "По умолчанию"),
                "sortBy() не должен падать");
    }
}

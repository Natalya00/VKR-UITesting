package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты панели вкладок */
public class TabPanelHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createTabPanel() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.TabPanel");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#tab-panel"));
    }

    @Test
    void testSelectTab() throws Exception {
        Object tabPanel = createTabPanel();

        assertDoesNotThrow(
                () -> invoke(tabPanel, "selectTab", "Настройки"),
                "selectTab() не должен падать при выборе существующей вкладки");

        String tabClass = $("#tab-settings").getAttribute("class");
        assertTrue(tabClass.contains("active"),
                "Вкладка 'Настройки' должна стать активной, текущий class: " + tabClass);
    }

    @Test
    void testSelectTabReturnsThis() throws Exception {
        Object tabPanel = createTabPanel();
        Object result = invoke(tabPanel, "selectTab", "Обзор");
        assertSame(tabPanel, result, "selectTab() должен возвращать this для цепочки вызовов");
    }

    @Test
    void testIsTabActiveReturnsTrue() throws Exception {
        Object tabPanel = createTabPanel();

        invoke(tabPanel, "selectTab", "Настройки");

        Object result = invoke(tabPanel, "isTabActive", "Настройки");
        assertTrue((Boolean) result, "isTabActive('Настройки') должен вернуть true после выбора");
    }

    @Test
    void testIsTabActiveReturnsFalse() throws Exception {
        Object tabPanel = createTabPanel();

        invoke(tabPanel, "selectTab", "Настройки");

        Object result = invoke(tabPanel, "isTabActive", "Обзор");
        assertFalse((Boolean) result, "isTabActive('Обзор') должен вернуть false когда активна 'Настройки'");
    }
}

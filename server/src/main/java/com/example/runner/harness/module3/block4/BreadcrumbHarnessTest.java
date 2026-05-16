package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты хлебных крошек */
public class BreadcrumbHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createBreadcrumb() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Breadcrumb");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#breadcrumb"));
    }

    @Test
    void testGetCurrentPathReturnsExactList() throws Exception {
        Object breadcrumb = createBreadcrumb();

        Object result = invoke(breadcrumb, "getCurrentPath");
        assertNotNull(result, "getCurrentPath() не должен возвращать null");
        assertTrue(result instanceof List,
                "getCurrentPath() должен возвращать List, а не "
                        + (result != null ? result.getClass().getSimpleName() : "null"));

        @SuppressWarnings("unchecked")
        List<String> path = (List<String>) result;

        assertTrue(path.contains("Главная"), "Текущий путь должен содержать 'Главная'");
        assertTrue(path.contains("Каталог"), "Текущий путь должен содержать 'Каталог'");
        assertTrue(path.contains("Электроника"), "Текущий путь должен содержать 'Электроника'");
    }

    @Test
    void testGetCurrentPathNotEmpty() throws Exception {
        Object breadcrumb = createBreadcrumb();

        Object result = invoke(breadcrumb, "getCurrentPath");
        @SuppressWarnings("unchecked")
        List<String> path = (List<String>) result;
        assertTrue(path.size() >= 2,
                "Текущий путь должен содержать как минимум 2 элемента, получено: " + path.size());
    }

    @Test
    void testNavigateTo() throws Exception {
        Object breadcrumb = createBreadcrumb();

        assertDoesNotThrow(
                () -> invoke(breadcrumb, "navigateTo", "Каталог"),
                "navigateTo() не должен падать при клике по существующему шагу");

        $("#breadcrumb").shouldBe(com.codeborne.selenide.Condition.exist, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testNavigateReturnsThis() throws Exception {
        Object breadcrumb = createBreadcrumb();
        Object result = invoke(breadcrumb, "navigateTo", "Товары");
        assertSame(breadcrumb, result, "navigateTo() должен возвращать this для цепочки вызовов");
    }
}

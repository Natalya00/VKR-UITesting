package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.*;

public class PaginationHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createPagination() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Pagination");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#pagination"));
    }

    private Object invokeInt(Object instance, String method, int arg) throws Exception {
        var m = instance.getClass().getMethod(method, int.class);
        return m.invoke(instance, arg);
    }

    @Test
    void testGetCurrentPage() throws Exception {
        Object pagination = createPagination();

        Object result = invoke(pagination, "getCurrentPage");
        assertNotNull(result, "getCurrentPage() не должен возвращать null");
        assertEquals(2, (int) result,
                "getCurrentPage() должен вернуть 2 (начальное состояние)");
    }

    @Test
    void testGoToPage() throws Exception {
        Object pagination = createPagination();

        invokeInt(pagination, "goToPage", 4);

        $("#pagination-info").shouldHave(text("Страница 4"));
    }

    @Test
    void testGoToPageReturnsThis() throws Exception {
        Object pagination = createPagination();
        Object result = invokeInt(pagination, "goToPage", 3);
        assertSame(pagination, result, "goToPage() должен возвращать this");
    }

    @Test
    void testIsFirstPage() throws Exception {
        Object pagination = createPagination();

        Object result = invoke(pagination, "isFirstPage");
        assertFalse((Boolean) result, "isFirstPage() должен вернуть false на странице 2");
    }

    @Test
    void testIsLastPage() throws Exception {
        Object pagination = createPagination();

        Object result = invoke(pagination, "isLastPage");
        assertFalse((Boolean) result, "isLastPage() должен вернуть false на странице 2 из 5");
    }
}

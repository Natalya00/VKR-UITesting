package com.example.runner.harness.module3.block2;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты базовой страницы списка */
public class BaseListPageHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/cat-characters";
    }

    @Test
    void testCatCharacterPageExtendsBaseListPage() throws Exception {
        Class<?> catPageClass = loadStudentClass("com.example.runner.pom.pages.CatCharacterPage");
        Class<?> baseListPageClass = loadStudentClass("com.example.runner.pom.pages.BaseListPage");

        assertTrue(baseListPageClass.isAssignableFrom(catPageClass),
                "CatCharacterPage должен наследоваться от BaseListPage");
    }

    @Test
    void testGetTitle() throws Exception {
        Class<?> catPageClass = loadStudentClass("com.example.runner.pom.pages.CatCharacterPage");
        Object catPage = catPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(catPage, "getTitle");
        assertNotNull(result, "getTitle() не должен возвращать null");
        assertInstanceOf(String.class, result, "getTitle() должен возвращать String");
    }

    @Test
    void testSearchReturnsThis() throws Exception {
        Class<?> catPageClass = loadStudentClass("com.example.runner.pom.pages.CatCharacterPage");
        Object catPage = catPageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(catPage, "search", "тест");
        assertNotNull(result, "search() не должен возвращать null");
        assertSame(catPage, result, "search() должен возвращать this для Fluent API");
    }
}

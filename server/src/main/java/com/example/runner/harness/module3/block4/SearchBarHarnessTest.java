package com.example.runner.harness.module3.block4;

import com.codeborne.selenide.ElementsCollection;
import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты строки поиска */
public class SearchBarHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createSearchBar() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.SearchBar");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#search-bar"));
    }

    @Test
    void testSearch() throws Exception {
        Object searchBar = createSearchBar();

        invoke(searchBar, "search", "тестовый запрос");

        String inputValue = $("#search-input").getAttribute("value");
        assertEquals("тестовый запрос", inputValue,
                "search() должен ввести текст в поле поиска");
    }

    @Test
    void testSearchReturnsThis() throws Exception {
        Object searchBar = createSearchBar();
        Object result = invoke(searchBar, "search", "запрос");
        assertSame(searchBar, result, "search() должен возвращать this для цепочки вызовов");
    }

    @Test
    void testClearSearch() throws Exception {
        Object searchBar = createSearchBar();

        $("#search-input").setValue("тестовый запрос");

        invoke(searchBar, "clearSearch");

        String value = $("#search-input").getAttribute("value");
        assertEquals("", value, "clearSearch() должен очистить поле поиска");
    }

    @Test
    void testClearReturnsThis() throws Exception {
        Object searchBar = createSearchBar();
        Object result = invoke(searchBar, "clearSearch");
        assertSame(searchBar, result, "clearSearch() должен возвращать this для цепочки вызовов");
    }

    @Test
    void testGetSuggestions() throws Exception {
        Object searchBar = createSearchBar();

        $("#search-input").setValue("тест");

        $(".suggestion-item").shouldBe(visible, java.time.Duration.ofSeconds(3));

        Object result = invoke(searchBar, "getSuggestions");
        assertNotNull(result, "getSuggestions() не должен возвращать null");
        assertTrue(result instanceof ElementsCollection,
                "getSuggestions() должен возвращать ElementsCollection, а не "
                        + (result != null ? result.getClass().getSimpleName() : "null"));

        ElementsCollection suggestions = (ElementsCollection) result;
        assertTrue(suggestions.size() > 0,
                "getSuggestions() должен вернуть непустую коллекцию при наличии подсказок");
    }
}

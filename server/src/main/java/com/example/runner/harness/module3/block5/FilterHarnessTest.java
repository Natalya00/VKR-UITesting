package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест фильтрации товаров */
public class FilterHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createProductPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testFilterByCategoryExists() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        java.lang.reflect.Method method = cls.getDeclaredMethod("filterByCategory", String.class);
        assertNotNull(method, "ProductPage должен содержать метод filterByCategory(String)");
    }

    @Test
    void testGetAllProductCategoriesReturnsList() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getAllProductCategories");
        assertNotNull(result, "getAllProductCategories() не должен возвращать null");
        assertTrue(result instanceof java.util.List, "getAllProductCategories() должен возвращать List");
    }

    @Test
    void testCategoryFilterExists() throws Exception {
        $("#category-filter").shouldBe(visible);
    }

    @Test
    void testGetAllCategoriesRealBehavior() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getAllProductCategories");
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<String> categories = (java.util.List<String>) result;
        assertTrue(categories.size() > 0, "Список категорий не должен быть пустым");
    }

    @Test
    void testFilterByCategoryRealBehavior() throws Exception {
        Object productPage = createProductPage();

        invoke(productPage, "filterByCategory", "electronics");

        Thread.sleep(500);

        var cards = $$(".product-card").filter(visible);
        assertTrue(cards.size() > 0, "После фильтрации должны остаться товары");

        for (var card : cards) {
            String category = card.getAttribute("data-product-category");
            assertEquals("electronics", category,
                    "Все отображаемые товары должны быть категории 'electronics', но найден: " + category);
        }
    }
}

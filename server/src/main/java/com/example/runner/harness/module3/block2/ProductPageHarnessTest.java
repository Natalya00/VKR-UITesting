package com.example.runner.harness.module3.block2;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты страницы ProductPage */
public class ProductPageHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createProductPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testFilterByCategory() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        Object page = createProductPage();
        Object result = invoke(page, "filterByCategory", "Электроника");
        assertNotNull(result);
        assertTrue(cls.isInstance(result),
                "filterByCategory() должен возвращать ProductPage");
    }

    @Test
    void testSelectFirstAvailableProduct() throws Exception {
        Class<?> itemClass = loadStudentClass("com.example.runner.pom.elements.ItemComponent");
        Object page = createProductPage();
        Object result = invoke(page, "selectFirstAvailableProduct");
        assertNotNull(result, "selectFirstAvailableProduct() не должен возвращать null");
        assertTrue(itemClass.isInstance(result),
                "Должен возвращать ItemComponent");
    }

    @Test
    void testGetProductCardByName() throws Exception {
        Object page = createProductPage();
        Object result = invoke(page, "getProductCardByName", "Ноутбук Pro 15");
        assertNotNull(result);
        if (result instanceof Optional) {
            assertTrue(((Optional<?>) result).isPresent(),
                    "getProductCardByName() должен найти существующий товар");
        }
    }

    @Test
    void testGetProductCardByNameNotFound() throws Exception {
        Object page = createProductPage();
        Object result = invoke(page, "getProductCardByName", "Несуществующий товар XYZ999");
        if (result instanceof Optional) {
            assertFalse(((Optional<?>) result).isPresent());
        } else {
            assertNull(result, "Для несуществующего товара должен вернуть null");
        }
    }

    @Test
    void testGetAllProductCards() throws Exception {
        Object page = createProductPage();
        Object result = invoke(page, "getAllProductCards");
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertFalse(((List<?>) result).isEmpty(),
                "getAllProductCards() должен вернуть хотя бы один товар");
    }
}

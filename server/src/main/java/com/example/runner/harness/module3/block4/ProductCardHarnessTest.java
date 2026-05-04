package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCardHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    @Test
    void testGetProductByIndex() throws Exception {
        Class<?> pageClass = loadStudentClass("com.example.runner.pom.pages.CatalogPage");
        Class<?> cardClass = loadStudentClass("com.example.runner.pom.components.ProductCard");
        Object page = pageClass.getDeclaredConstructor().newInstance();

        var method = pageClass.getDeclaredMethod("getProductByIndex", int.class);
        Object card = method.invoke(page, 0);

        assertNotNull(card, "getProductByIndex(0) не должен возвращать null");
        assertTrue(cardClass.isInstance(card), "Должен вернуть ProductCard");
    }
}

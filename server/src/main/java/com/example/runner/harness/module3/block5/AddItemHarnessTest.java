package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест добавления товара на ProductPage */
public class AddItemHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createProductPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testAddProductExists() throws Exception {
        Class<?> productPageClass = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        Class<?> productClass = loadStudentClass("com.example.runner.tests.Product");
        java.lang.reflect.Method method = productPageClass.getDeclaredMethod("addProduct", productClass);
        assertNotNull(method, "ProductPage должен содержать метод addProduct(Product)");
    }

    @Test
    void testGetProductNamesReturnsList() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getProductNames");
        assertNotNull(result, "getProductNames() не должен возвращать null");
        assertTrue(result instanceof java.util.List, "getProductNames() должен возвращать List");
    }

    @Test
    void testAddButtonExists() throws Exception {
        $("#add-product-btn").shouldBe(visible);
    }

    @Test
    void testGetProductNamesRealBehavior() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getProductNames");
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<String> names = (java.util.List<String>) result;
        assertTrue(names.size() > 0, "getProductNames() должен вернуть хотя бы один товар");
        assertTrue(names.contains("Ноутбук Pro 15"), "Список должен содержать 'Ноутбук Pro 15'");
    }

    @Test
    void testAddProductRealBehavior() throws Exception {
        Object productPage = createProductPage();

        Class<?> productClass = loadStudentClass("com.example.runner.tests.Product");
        Object product = productClass.getConstructor(String.class, String.class)
                .newInstance("Harness Test Item", "electronics");

        Object before = invoke(productPage, "getProductNames");
        int countBefore = ((java.util.List<?>) before).size();

        invoke(productPage, "addProduct", product);

        Thread.sleep(500);

        Object after = invoke(productPage, "getProductNames");
        java.util.List<?> namesAfter = (java.util.List<?>) after;
        assertTrue(namesAfter.size() > countBefore,
                "После addProduct() количество товаров должно увеличиться");
        assertTrue(namesAfter.contains("Harness Test Item"),
                "Список должен содержать 'Harness Test Item' после добавления");
    }
}

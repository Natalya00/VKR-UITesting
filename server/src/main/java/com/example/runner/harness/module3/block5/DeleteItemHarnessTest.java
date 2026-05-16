package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест удаления товара */
public class DeleteItemHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createProductPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testDeleteProductExists() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProductPage");
        java.lang.reflect.Method method = cls.getDeclaredMethod("deleteProduct", String.class);
        assertNotNull(method, "ProductPage должен содержать метод deleteProduct(String)");
    }

    @Test
    void testGetProductNamesReturnsList() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getProductNames");
        assertNotNull(result, "getProductNames() не должен возвращать null");
        assertTrue(result instanceof java.util.List, "getProductNames() должен возвращать List");
    }

    @Test
    void testDeleteButtonsExist() throws Exception {
        $("[data-testid^='delete-']").shouldBe(visible);
    }

    @Test
    void testGetProductNamesRealBehavior() throws Exception {
        Object productPage = createProductPage();
        Object result = invoke(productPage, "getProductNames");
        assertNotNull(result);
        assertTrue(result instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<String> names = (java.util.List<String>) result;
        assertTrue(names.size() > 0, "Список имён не должен быть пустым");
    }

    @Test
    void testDeleteProductRealBehavior() throws Exception {
        Object productPage = createProductPage();

        Object before = invoke(productPage, "getProductNames");
        java.util.List<?> namesBefore = (java.util.List<?>) before;
        assertTrue(namesBefore.contains("Ноутбук Pro 15"),
                "Перед удалением 'Ноутбук Pro 15' должен быть в списке");

        invoke(productPage, "deleteProduct", "Ноутбук Pro 15");

        Thread.sleep(500);

        Object after = invoke(productPage, "getProductNames");
        java.util.List<?> namesAfter = (java.util.List<?>) after;
        assertFalse(namesAfter.contains("Ноутбук Pro 15"),
                "'Ноутбук Pro 15' должен исчезнуть из списка после deleteProduct()");
    }
}

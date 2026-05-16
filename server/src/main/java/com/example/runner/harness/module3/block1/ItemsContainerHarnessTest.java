package com.example.runner.harness.module3.block1;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты контейнера ItemsContainer */
public class ItemsContainerHarnessTest extends AbstractHarnessTest {

    private static final String CONTAINER_XPATH = "//div[@data-testid='items-container']";
    private static final String TITLE_LAPTOP  = "Ноутбук Pro 15";
    private static final String TITLE_PHONE   = "Смартфон X200";

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/block1/items";
    }

    private Object createContainer() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.elements.ItemsContainer");
        Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(CONTAINER_XPATH, "");
    }

    @Test
    void testGetItems() throws Exception {
        Object container = createContainer();
        Object items = invoke(container, "getItems");

        assertNotNull(items);
        assertTrue(items instanceof List);
        assertFalse(((List<?>) items).isEmpty(),
                "getItems() должен возвращать хотя бы один элемент");
    }

    @Test
    void testGetItemByTitle() throws Exception {
        Object container = createContainer();
        Object item = invoke(container, "getItemByTitle", TITLE_LAPTOP);

        assertNotNull(item,
                "getItemByTitle(\"" + TITLE_LAPTOP + "\") должен вернуть не null");
    }

    @Test
    void testHasItemTrue() throws Exception {
        Object container = createContainer();
        Object result = invoke(container, "hasItem", TITLE_PHONE);

        assertTrue((Boolean) result,
                "hasItem() должен вернуть true для \"" + TITLE_PHONE + "\"");
    }

    @Test
    void testHasItemFalse() throws Exception {
        Object container = createContainer();
        Object result = invoke(container, "hasItem", "Несуществующий товар XYZ");

        assertFalse((Boolean) result,
                "hasItem() должен вернуть false для несуществующего элемента");
    }

    @Test
    void testItemComponentGetTitle() throws Exception {
        Object container = createContainer();
        Object item = invoke(container, "getItemByTitle", TITLE_LAPTOP);

        String title = (String) invoke(item, "getTitle");
        assertEquals(TITLE_LAPTOP, title,
                "getTitle() должен вернуть \"" + TITLE_LAPTOP + "\"");
    }

    @Test
    void testClickActionButton() throws Exception {
        Object container = createContainer();
        Object item = invoke(container, "getItemByTitle", TITLE_LAPTOP);

        assertDoesNotThrow(() -> invoke(item, "clickActionButton", "Купить"),
                "clickActionButton() должен найти кнопку по тексту и кликнуть по ней");
    }
}

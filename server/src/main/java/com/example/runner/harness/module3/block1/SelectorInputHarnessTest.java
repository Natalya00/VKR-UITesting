package com.example.runner.harness.module3.block1;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;


public class SelectorInputHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/block1/elements";
    }

    private Object createSelectorInput(String xpath) throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.elements.SelectorInput");
        Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(xpath, "");
    }

    @Test
    void testSelectByVisibleText() throws Exception {
        Object selector = createSelectorInput("//*[@id='country']");

        assertDoesNotThrow(
                () -> invoke(selector, "select", "Россия"),
                "select() должен выполнять выбор значения без исключений");

        String selectedValue = $("#country").getAttribute("value");
        assertEquals("ru", selectedValue,
                "После select(\"Россия\") должно быть выбрано значение 'ru'");
    }

    @Test
    void testSelectReturnsThis() throws Exception {
        Object selector = createSelectorInput("//*[@id='country']");

        Object result = invoke(selector, "select", "США");

        assertSame(selector, result,
                "Метод select() должен возвращать this для поддержки цепочки вызовов");
    }

    @Test
    void testSelectorInputExtendsInput() throws Exception {
        Class<?> selectorClass = loadStudentClass("com.example.runner.pom.elements.SelectorInput");
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        assertTrue(
                inputClass.isAssignableFrom(selectorClass),
                "SelectorInput должен наследоваться от Input");
    }

    @Test
    void testSelectCity() throws Exception {
        Object selector = createSelectorInput("//*[@id='city']");

        assertDoesNotThrow(
                () -> invoke(selector, "select", "Санкт-Петербург"),
                "select() должен работать с любым select на странице");

        String selectedValue = $("#city").getAttribute("value");
        assertEquals("spb", selectedValue,
                "После select(\"Санкт-Петербург\") должно быть выбрано значение 'spb'");
    }

    @Test
    void testSelectUserBehavior() throws Exception {
        Object selector = createSelectorInput("//*[@id='country']");

        String beforeValue = $("#country").getAttribute("value");
        assertEquals("", beforeValue,
                "Перед тестом select должен иметь пустое значение");

        invoke(selector, "select", "Германия");

        String afterValue = $("#country").getAttribute("value");
        assertEquals("de", afterValue,
                "select() должен реально выбирать значение из списка");
    }
}

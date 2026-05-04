package com.example.runner.harness.module3.block1;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;


public class ButtonHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/block1/elements";
    }

    private Object createButton(String xpath) throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.elements.Button");
        Constructor<?> ctor = cls.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(xpath, "");
    }

    @Test
    void testClickOnVisibleButton() throws Exception {
        Object button = createButton("//*[@id='visible-btn']");

        assertDoesNotThrow(
                () -> invoke(button, "click"),
                "click() не должен выбрасывать исключение для видимой кнопки");

        assertEquals("true",
                $("#visible-btn").getAttribute("data-clicked"),
                "После click() атрибут data-clicked должен стать 'true'");
    }

    @Test
    void testClickOnHiddenButtonThrows() throws Exception {
        Object button = createButton("//*[@id='hidden-btn']");

        Exception thrown = assertThrows(
                Exception.class,
                () -> invoke(button, "click"),
                "click() должен выбрасывать исключение для скрытой кнопки");

        Throwable root = thrown.getCause() != null ? thrown.getCause() : thrown;
        assertEquals(
                "IllegalStateException",
                root.getClass().getSimpleName(),
                "Должен быть выброшен IllegalStateException, а не " + root.getClass().getSimpleName());
    }

    @Test
    void testButtonExtendsAbstractClickableElement() throws Exception {
        Class<?> buttonClass = loadStudentClass("com.example.runner.pom.elements.Button");
        Class<?> abstractClass = loadStudentClass(
                "com.example.runner.pom.elements.AbstractClickableElement");

        assertTrue(
                abstractClass.isAssignableFrom(buttonClass),
                "Button должен наследоваться от AbstractClickableElement");
    }

    @Test
    void testButtonImplementsClickable() throws Exception {
        Class<?> buttonClass = loadStudentClass("com.example.runner.pom.elements.Button");
        Class<?> clickableInterface = loadStudentClass(
                "com.example.runner.pom.elements.Clickable");

        assertTrue(
                clickableInterface.isAssignableFrom(buttonClass),
                "Button должен реализовывать интерфейс Clickable");
    }

    @Test
    void testClickCallsSuper() throws Exception {
        Object button = createButton("//*[@id='visible-btn']");

        invoke(button, "click");

        String attr = $("#visible-btn").getAttribute("data-clicked");
        assertEquals("true", attr,
                "super.click() должен быть вызван — элемент должен получить клик");
    }
}

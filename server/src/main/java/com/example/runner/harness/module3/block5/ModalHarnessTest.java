package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.exist;
import static org.junit.jupiter.api.Assertions.*;

public class ModalHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createModalPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testOpenModalExists() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        java.lang.reflect.Method method = cls.getDeclaredMethod("openModal");
        assertNotNull(method, "ModalPage должен содержать метод openModal()");
    }

    @Test
    void testFillFieldExists() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        java.lang.reflect.Method method = cls.getDeclaredMethod("fillField", String.class);
        assertNotNull(method, "ModalPage должен содержать метод fillField(String)");
    }

    @Test
    void testSubmitExists() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        java.lang.reflect.Method method = cls.getDeclaredMethod("submit");
        assertNotNull(method, "ModalPage должен содержать метод submit()");
    }

    @Test
    void testIsClosedReturnsBoolean() throws Exception {
        Object modalPage = createModalPage();
        Object result = invoke(modalPage, "isClosed");
        assertNotNull(result, "isClosed() не должен возвращать null");
        assertInstanceOf(Boolean.class, result, "isClosed() должен возвращать boolean");
    }

    @Test
    void testOpenModalButtonExists() throws Exception {
        $("#open-modal-btn").shouldBe(visible);
    }

    @Test
    void testModalOpens() throws Exception {
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testFillAndSubmitRealBehavior() throws Exception {
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(3));

        Object modalPage = createModalPage();

        invoke(modalPage, "fillField", "test value");

        assertEquals("test value", $("#modal-input").getValue(),
                "fillField() должен заполнить поле ввода");

        invoke(modalPage, "submit");

        $("#modal-window").shouldNotBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testOpenModalRealBehavior() throws Exception {
        $("#modal-window").shouldNotBe(visible, java.time.Duration.ofSeconds(2));

        Object modalPage = createModalPage();

        invoke(modalPage, "openModal");

        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testIsClosedRealBehavior() throws Exception {
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(3));

        Object modalPage = createModalPage();

        Object resultOpen = invoke(modalPage, "isClosed");
        assertFalse((Boolean) resultOpen, "isClosed() должен вернуть false когда модалка открыта");

        invoke(modalPage, "submit");
        $("#modal-window").shouldNotBe(visible, java.time.Duration.ofSeconds(3));

        Object resultClosed = invoke(modalPage, "isClosed");
        assertTrue((Boolean) resultClosed, "isClosed() должен вернуть true когда модалка закрыта");
    }
}

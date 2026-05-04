package com.example.runner.harness.module3.block2;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;


public class ModalHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/products";
    }

    private Object createModalPage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        Constructor<?> ctor = cls.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        return ctor.newInstance("//div[@id='modal-window']");
    }

    private void ensureModalClosed() {
        String display = $("#modal-overlay").getCssValue("display");
        if (!display.equals("none")) {
            try {
                $("#modal-close").click();
                Thread.sleep(300);
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testOpenModal() throws Exception {
        ensureModalClosed();

        Object modalPage = createModalPage();
        invoke(modalPage, "openModal");

        String display = $("#modal-overlay").getCssValue("display");
        assertNotEquals("none", display, "После openModal() модалка должна стать видимой");

        $("#modal-close").click();
    }

    @Test
    void testFillModal() throws Exception {
        ensureModalClosed();
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(10));
        Thread.sleep(500);

        Object modalPage = createModalPage();
        invoke(modalPage, "fillModal", "Test Value");

        String value = $("#modal-input").getValue();
        assertEquals("Test Value", value, "fillModal() должен ввести текст в поле");

        $("#modal-close").click();
    }

    @Test
    void testSubmitModal() throws Exception {
        ensureModalClosed();
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(10));
        Thread.sleep(500);

        Object modalPage = createModalPage();
        Object result = invoke(modalPage, "submitModal");

        assertNotNull(result, "submitModal() не должен возвращать null");
        assertSame(modalPage, result, "submitModal() должен возвращать this");

        Thread.sleep(500);
        String display = $("#modal-overlay").getCssValue("display");
        assertEquals("none", display, "После submitModal() модалка должна закрыться");
    }

    @Test
    void testModalClose() throws Exception {
        ensureModalClosed();
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(10));
        Thread.sleep(500);

        Object modalPage = createModalPage();

        String displayBefore = $("#modal-overlay").getCssValue("display");
        assertNotEquals("none", displayBefore, "Модалка должна быть открыта");

        invoke(modalPage, "close");

        String displayAfter = $("#modal-overlay").getCssValue("display");
        assertEquals("none", displayAfter, "После close() модалка должна закрыться");
    }

    @Test
    void testModalCloseReturnsThis() throws Exception {
        ensureModalClosed();
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(10));
        Thread.sleep(500);

        Object modalPage = createModalPage();
        Object result = invoke(modalPage, "close");

        assertSame(modalPage, result, "close() должен возвращать this");
    }

    @Test
    void testIsModalVisibleClosed() throws Exception {
        ensureModalClosed();

        Object modalPage = createModalPage();
        Object result = invoke(modalPage, "isModalVisible");

        assertNotNull(result);
        assertFalse((Boolean) result, "isModalVisible() → false когда модалка закрыта");
    }

    @Test
    void testIsModalVisibleOpen() throws Exception {
        ensureModalClosed();
        $("#open-modal-btn").click();
        $("#modal-window").shouldBe(visible, java.time.Duration.ofSeconds(10));
        Thread.sleep(500);

        Object modalPage = createModalPage();
        Object result = invoke(modalPage, "isModalVisible");

        assertNotNull(result);
        assertTrue((Boolean) result, "isModalVisible() → true когда модалка открыта");

        $("#modal-close").click();
    }

    @Test
    void testModalPageExtendsBasePage() throws Exception {
        Class<?> modalPageClass = loadStudentClass("com.example.runner.pom.pages.ModalPage");
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");

        assertTrue(basePageClass.isAssignableFrom(modalPageClass),
                "ModalPage должен наследоваться от BasePage");
    }
}

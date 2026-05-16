package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.text;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты виджета формы */
public class FormWidgetHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createFormWidget() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.FormWidget");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#form-widget"));
    }

    @Test
    void testFillAndSubmit() throws Exception {
        Object widget = createFormWidget();

        invoke(widget, "fillAndSubmit", "Тестовый пользователь", "Электроника");

        $("#form-success").shouldBe(visible, java.time.Duration.ofSeconds(3));
        $("#form-success").shouldHave(text("Тестовый пользователь"));
        $("#form-success").shouldHave(text("Электроника"));
    }

    @Test
    void testSubmitForm() throws Exception {
        Class<?> pageClass = loadStudentClass("com.example.runner.pom.pages.FormPage");
        Constructor<?> pageCtor = pageClass.getDeclaredConstructor();
        Object page = pageCtor.newInstance();

        invoke(page, "submitForm", "Пользователь теста", "Книги");

        $("#form-success").shouldBe(visible, java.time.Duration.ofSeconds(3));
        $("#form-success").shouldHave(text("Пользователь теста"));
    }

    @Test
    void testSubmitFormReturnsThis() throws Exception {
        Class<?> pageClass = loadStudentClass("com.example.runner.pom.pages.FormPage");
        Constructor<?> pageCtor = pageClass.getDeclaredConstructor();
        Object page = pageCtor.newInstance();

        Object result = invoke(page, "submitForm", "Тест", "Одежда");
        assertSame(page, result, "submitForm() должен возвращать this");
    }
}

package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты аккордеона */
public class AccordionHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createAccordion() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Accordion");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#accordion"));
    }

    private Object invokeInt(Object instance, String method, int arg) throws Exception {
        var m = instance.getClass().getMethod(method, int.class);
        return m.invoke(instance, arg);
    }

    private boolean invokeIntBoolean(Object instance, String method, int arg) throws Exception {
        var m = instance.getClass().getMethod(method, int.class);
        return (Boolean) m.invoke(instance, arg);
    }

    @Test
    void testExpandSection() throws Exception {
        Object accordion = createAccordion();

        $("#accordion-content-1").shouldNotBe(exist);

        invokeInt(accordion, "expandSection", 1);

        $("#accordion-content-1").shouldBe(exist, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testExpandReturnsThis() throws Exception {
        Object accordion = createAccordion();
        Object result = invokeInt(accordion, "expandSection", 1);
        assertSame(accordion, result, "expandSection() должен возвращать this");
    }

    @Test
    void testCollapseSection() throws Exception {
        Object accordion = createAccordion();

        $("#accordion-content-0").shouldBe(exist);

        invokeInt(accordion, "collapseSection", 0);

        $("#accordion-content-0").should(not(exist), java.time.Duration.ofSeconds(3));
    }

    @Test
    void testCollapseReturnsThis() throws Exception {
        Object accordion = createAccordion();
        Object result = invokeInt(accordion, "collapseSection", 0);
        assertSame(accordion, result, "collapseSection() должен возвращать this");
    }

    @Test
    void testIsSectionExpandedTrue() throws Exception {
        Object accordion = createAccordion();

        boolean result = invokeIntBoolean(accordion, "isSectionExpanded", 0);
        assertTrue(result, "isSectionExpanded(0) должен вернуть true");
    }

    @Test
    void testIsSectionExpandedFalse() throws Exception {
        Object accordion = createAccordion();

        boolean result = invokeIntBoolean(accordion, "isSectionExpanded", 1);
        assertFalse(result, "isSectionExpanded(1) должен вернуть false");
    }
}

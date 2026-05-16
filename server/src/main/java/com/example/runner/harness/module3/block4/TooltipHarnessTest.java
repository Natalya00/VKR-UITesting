package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Condition.exist;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты всплывающих подсказок */
public class TooltipHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createTooltip() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.Tooltip");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#tooltip-container"));
    }

    @Test
    void testHoverShowsTooltip() throws Exception {
        Object tooltip = createTooltip();

        $("#tooltip-content").shouldNotBe(exist);

        invoke(tooltip, "hoverToShow");

        $("#tooltip-content").shouldBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testGetText() throws Exception {
        Object tooltip = createTooltip();

        invoke(tooltip, "hoverToShow");

        Object result = invoke(tooltip, "getText");
        assertNotNull(result, "getText() не должен возвращать null");
        assertTrue(result instanceof String, "getText() должен возвращать String");
        assertTrue(((String) result).length() > 0, "getText() должен вернуть непустой текст");
    }

    @Test
    void testIsDisplayedReturnsTrue() throws Exception {
        Object tooltip = createTooltip();

        invoke(tooltip, "hoverToShow");

        Object result = invoke(tooltip, "isDisplayed");
        assertTrue((Boolean) result, "isDisplayed() должен вернуть true после hover");
    }

    @Test
    void testIsDisplayedReturnsFalse() throws Exception {
        Object tooltip = createTooltip();

        Object result = invoke(tooltip, "isDisplayed");
        assertFalse((Boolean) result, "isDisplayed() должен вернуть false до hover");
    }
}

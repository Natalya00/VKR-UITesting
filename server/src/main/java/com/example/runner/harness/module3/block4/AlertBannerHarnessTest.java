package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты баннера уведомлений */
public class AlertBannerHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createAlertBanner() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.AlertBanner");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#alert-banner"));
    }

    @Test
    void testGetMessage() throws Exception {
        Object banner = createAlertBanner();

        Object result = invoke(banner, "getMessage");
        assertNotNull(result, "getMessage() не должен возвращать null");
        assertTrue(result instanceof String,
                "getMessage() должен возвращать String, а не " + result.getClass().getSimpleName());

        String message = (String) result;
        assertTrue(message.length() > 0, "getMessage() должен вернуть непустой текст");
    }

    @Test
    void testDismiss() throws Exception {
        Object banner = createAlertBanner();

        $("#alert-banner").shouldBe(visible);

        invoke(banner, "dismiss");

        $("#alert-banner").shouldNotBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testDismissReturnsThis() throws Exception {
        Object banner = createAlertBanner();
        Object result = invoke(banner, "dismiss");
        assertSame(banner, result, "dismiss() должен возвращать this для цепочки вызовов");
    }

    @Test
    void testIsDisplayedReturnsTrue() throws Exception {
        Object banner = createAlertBanner();

        Object result = invoke(banner, "isDisplayed");
        assertTrue((Boolean) result, "isDisplayed() должен вернуть true для видимого баннера");
    }

    @Test
    void testWaitForDismiss() throws Exception {
        Object banner = createAlertBanner();

        var method = banner.getClass().getMethod("waitForDismiss", long.class);
        assertNotNull(method, "waitForDismiss(long) должен существовать");

        invoke(banner, "dismiss");

        assertDoesNotThrow(() -> method.invoke(banner, 1000L),
                "waitForDismiss() не должен падать на уже скрытом элементе");
    }

    @Test
    void testNotificationListDismissAll() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.NotificationList");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);

        $("#alert-banner").shouldBe(visible);

        Object list = ctor.newInstance($("#alert-banner").ancestor("section"));
        invoke(list, "dismissAll");

        $("#alert-banner").shouldNotBe(visible, java.time.Duration.ofSeconds(3));
    }

    @Test
    void testNotificationListDismissAllReturnsThis() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.NotificationList");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);

        Object list = ctor.newInstance($("#alert-banner").ancestor("section"));
        Object result = invoke(list, "dismissAll");
        assertSame(list, result, "dismissAll() должен возвращать this");
    }
}

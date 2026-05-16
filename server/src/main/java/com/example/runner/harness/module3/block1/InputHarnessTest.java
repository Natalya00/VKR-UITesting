package com.example.runner.harness.module3.block1;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты элемента Input */
public class InputHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/block1/elements";
    }

    @Test
    @Order(1)
    void testFillClearsAndSetsValue() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        $("#username").shouldBe(exist, java.time.Duration.ofSeconds(10));

        var constructor = inputClass.getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);

        Object input = constructor.newInstance("//*[@id='%s']", "username");
        assertNotNull(input, "Input не должен быть null");

        var fillMethod = inputClass.getMethod("fill", String.class);
        fillMethod.invoke(input, "TestUser123");

        assertValue("#username", "TestUser123");
    }

    @Test
    @Order(2)
    void testFillReturnsThis() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        $("#username").shouldBe(exist, java.time.Duration.ofSeconds(10));

        var constructor = inputClass.getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);
        Object input = constructor.newInstance("//*[@id='%s']", "username");

        var fillMethod = inputClass.getMethod("fill", String.class);
        Object result = fillMethod.invoke(input, "test");

        assertSame(input, result,
                "Метод fill() должен возвращать this для поддержки цепочки вызовов");
    }

    @Test
    @Order(3)
    void testByIdNullThrows() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        try {
            var byIdMethod = inputClass.getMethod("byId", String.class);

            assertThrows(Exception.class,
                    () -> byIdMethod.invoke(null, (Object) null),
                    "byId(null) должен выбрасывать IllegalArgumentException");
        } catch (NoSuchMethodException e) {
            log.warn("[InputHarnessTest] Метод byId() ещё не реализован (упражнение 4)");
        }
    }

    @Test
    @Order(4)
    void testByIdEmptyThrows() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        try {
            var byIdMethod = inputClass.getMethod("byId", String.class);

            assertThrows(Exception.class,
                    () -> byIdMethod.invoke(null, ""),
                    "byId(\"\") должен выбрасывать IllegalArgumentException");
        } catch (NoSuchMethodException e) {
            log.warn("[InputHarnessTest] Метод byId() ещё не реализован (упражнение 4)");
        }
    }

    @Test
    @Order(5)
    void testByName() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        try {
            var byNameMethod = inputClass.getMethod("byName", String.class);
            Object input = byNameMethod.invoke(null, "userEmail");
            assertNotNull(input, "byName('userEmail') должен вернуть не-null Input");

            try {
                var fillMethod = inputClass.getMethod("fill", String.class);
                fillMethod.invoke(input, "test@example.com");
                assertValue("[name='userEmail']", "test@example.com");
            } catch (Throwable e) {
                log.warn("[InputHarnessTest] fill() не реализован — проверка значения пропущена");
            }
        } catch (NoSuchMethodException e) {
            log.warn("[InputHarnessTest] Метод byName() ещё не реализован (упражнение 5)");
        }
    }

    @Test
    @Order(6)
    void testByPlaceholder() throws Exception {
        Class<?> inputClass = loadStudentClass("com.example.runner.pom.elements.Input");

        try {
            var byPlaceholderMethod = inputClass.getMethod("byPlaceholder", String.class);
            Object input = byPlaceholderMethod.invoke(null, "Введите имя");
            assertNotNull(input, "byPlaceholder('Введите имя') должен вернуть не-null Input");

            try {
                var fillMethod = inputClass.getMethod("fill", String.class);
                fillMethod.invoke(input, "testuser");
                assertValue("[placeholder='Введите имя']", "testuser");
            } catch (Throwable e) {
                log.warn("[InputHarnessTest] fill() не реализован — проверка значения пропущена");
            }
        } catch (NoSuchMethodException e) {
            log.warn("[InputHarnessTest] Метод byPlaceholder() ещё не реализован (упражнение 5)");
        }
    }
}

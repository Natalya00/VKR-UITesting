package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.Optional;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты табличного компонента */
public class TableComponentHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createTableComponent(String selector) throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.TableComponent");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($(selector));
    }

    @Test
    void testIsEmptyTrue() throws Exception {
        Object table = createTableComponent("#empty-table");
        Object result = invoke(table, "isEmpty");
        assertTrue((Boolean) result, "isEmpty() должен вернуть true для пустой таблицы");
    }

    @Test
    void testIsEmptyFalse() throws Exception {
        Object table = createTableComponent("#data-table");
        Object result = invoke(table, "isEmpty");
        assertFalse((Boolean) result, "isEmpty() должен вернуть false для таблицы с данными");
    }

    @Test
    void testGetOptionalRowFound() throws Exception {
        Object table = createTableComponent("#data-table");
        Object result = invoke(table, "getOptionalRow", "Иванов");
        assertNotNull(result);
        if (result instanceof Optional) {
            assertTrue(((Optional<?>) result).isPresent(),
                    "getOptionalRow() должен найти строку 'Иванов'");
        }
    }

    @Test
    void testGetOptionalRowNotFound() throws Exception {
        Object table = createTableComponent("#data-table");
        Object result = invoke(table, "getOptionalRow", "НесуществующийПользователь");
        if (result instanceof Optional) {
            assertFalse(((Optional<?>) result).isPresent());
        } else {
            assertNull(result);
        }
    }
}

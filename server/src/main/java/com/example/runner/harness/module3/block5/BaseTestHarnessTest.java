package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BaseTestHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/";
    }

    @Test
    void testBaseTestExists() throws Exception {
        Class<?> baseTestClass = loadStudentClass("com.example.runner.tests.BaseTest");
        assertNotNull(baseTestClass, "Класс BaseTest не найден");
    }

    @Test
    void testSetUpMethodExists() throws Exception {
        Class<?> baseTestClass = loadStudentClass("com.example.runner.tests.BaseTest");
        var method = baseTestClass.getMethod("setUp");
        assertNotNull(method);
        assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "Метод setUp() должен быть static");
    }
}

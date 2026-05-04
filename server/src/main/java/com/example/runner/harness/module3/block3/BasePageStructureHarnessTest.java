package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class BasePageStructureHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testConstructorInitializesFields() throws Exception {
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");

        Constructor<?> ctor = basePageClass.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        assertNotNull(ctor, "Конструктор BasePage(String, String) должен существовать");

        Object page = ctor.newInstance("//footer", "Тестовый заголовок");

        Method getTitleMethod = basePageClass.getMethod("getTitle");
        Object titleResult = getTitleMethod.invoke(page);
        assertNotNull(titleResult, "getTitle() не должен возвращать null");
        assertEquals("Тестовый заголовок", titleResult, "getTitle() должен возвращать переданный title");
    }

    @Test
    void testFindElement() throws Exception {
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");
        Constructor<?> ctor = basePageClass.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);

        Object page = ctor.newInstance("//footer", "Test");

        Method findElementMethod = basePageClass.getDeclaredMethod("findElement", String.class);
        findElementMethod.setAccessible(true);

        Object element = findElementMethod.invoke(page, ".//a");

        assertNotNull(element, "findElement() не должен возвращать null. Убедитесь, что используете baseContainer.$x(xpath)");
    }
}

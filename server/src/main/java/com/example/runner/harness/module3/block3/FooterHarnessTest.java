package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты компонента Footer */
public class FooterHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    private Object createBasePage() throws Exception {
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");
        java.lang.reflect.Constructor<?> ctor = basePageClass.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        return ctor.newInstance("//footer");
    }

    @Test
    void testScrollToFooter() throws Exception {
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");
        var method = basePageClass.getDeclaredMethod("scrollToFooter");
        assertNotNull(method, "scrollToFooter() должен существовать");
        assertTrue(java.lang.reflect.Modifier.isProtected(method.getModifiers()),
                "scrollToFooter() должен быть protected");

        Object basePage = createBasePage();

        try {
            method.setAccessible(true);
            method.invoke(basePage);
        } catch (java.lang.reflect.InvocationTargetException e) {
            fail("scrollToFooter() выбросил исключение. Проверьте executeJavaScript(\"arguments[0].scrollIntoView()\", footer): " + e.getCause());
        }
    }

    @Test
    void testIsFooterVisible() throws Exception {
        Object basePage = createBasePage();
        Object result = invoke(basePage, "isFooterVisible");
        assertNotNull(result, "isFooterVisible() не должен возвращать null");
        assertInstanceOf(Boolean.class, result, "isFooterVisible() должен возвращать boolean");
    }

    @Test
    void testGetFooterText() throws Exception {
        Object basePage = createBasePage();
        Object result = invoke(basePage, "getFooterText");
        if (result != null) {
            assertInstanceOf(String.class, result, "getFooterText() должен возвращать String");
        }
    }
}

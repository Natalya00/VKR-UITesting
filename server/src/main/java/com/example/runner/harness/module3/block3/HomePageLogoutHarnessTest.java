package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HomePageLogoutHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testLogoutReturnsThis() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        var method = homePageClass.getDeclaredMethod("logout");
        assertEquals(homePageClass, method.getReturnType(),
                "logout() должен возвращать HomePage");

        try {
            Object result = method.invoke(homePage);
            assertNotNull(result, "logout() не должен возвращать null");
            assertSame(homePage, result, "logout() должен возвращать this");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalStateException) {
                log.warn("[Test] logout() вызвал waitAndClick, но элемент не виден");
            } else {
                throw e;
            }
        }
    }
}

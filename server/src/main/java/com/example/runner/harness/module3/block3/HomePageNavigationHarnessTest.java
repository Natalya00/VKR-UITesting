package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Harness-тесты навигации с HomePage */
public class HomePageNavigationHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testGoToProfile() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        try {
            Object result = invoke(homePage, "goToProfile");
            assertNotNull(result, "goToProfile() не должен возвращать null");
            assertTrue(profilePageClass.isInstance(result),
                    "goToProfile() должен возвращать ProfilePage");
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalStateException) {
                log.warn("[Test] goToProfile() проверил видимость, но элемент не виден");
            } else {
                throw e;
            }
        }
    }

    @Test
    void testCheckPageLoaded() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "checkPageLoaded");

        assertNotNull(result, "checkPageLoaded() не должен возвращать null");
        assertInstanceOf(Boolean.class, result,
                "checkPageLoaded() должен возвращать boolean");
    }
}

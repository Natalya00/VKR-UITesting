package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentsHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testClickLogoInHeader() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "clickLogoInHeader");

        assertNotNull(result, "clickLogoInHeader() не должен возвращать null");
        assertSame(homePage, result, "clickLogoInHeader() должен возвращать this");
    }

    @Test
    void testNavigateToProfile() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "navigateToProfile");

        assertNotNull(result, "navigateToProfile() не должен возвращать null");
        assertSame(homePage, result, "navigateToProfile() должен возвращать this");
    }

    @Test
    void testClickSocialLinkInFooter() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Object homePage = homePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(homePage, "clickSocialLinkInFooter", "vk");

        assertNotNull(result, "clickSocialLinkInFooter() не должен возвращать null");
        assertSame(homePage, result, "clickSocialLinkInFooter() должен возвращать this");
    }
}

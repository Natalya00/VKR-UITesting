package com.example.runner.harness.module3.block2;

import com.codeborne.selenide.Selenide;
import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/** Harness-тесты страницы HomePage */
public class HomePageHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testGoToProfileReturnsProfilePage() throws Exception {
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");

        Object homePage = homePageClass.getDeclaredConstructor().newInstance();
        Object result = invoke(homePage, "goToProfile");

        assertNotNull(result);
        assertTrue(profilePageClass.isInstance(result),
                "goToProfile() должен возвращать объект ProfilePage");
    }

    @Test
    void testHomePageExtendsBasePage() throws Exception {
        Class<?> homePageClass = loadStudentClass("com.example.runner.pom.pages.HomePage");
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");

        assertTrue(basePageClass.isAssignableFrom(homePageClass),
                "HomePage должен наследоваться от BasePage");
    }

    @Test
    void testGetUserName() throws Exception {
        Selenide.open("/test-harness/module3/profile");

        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        Object profilePage = profilePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(profilePage, "getUserName");
        assertNotNull(result, "getUserName() не должен возвращать null");
        assertInstanceOf(String.class, result, "getUserName() должен возвращать String");
    }

    @Test
    void testEditName() throws Exception {
        Selenide.open("/test-harness/module3/profile");

        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        Object profilePage = profilePageClass.getDeclaredConstructor().newInstance();

        Object result = invoke(profilePage, "editName", "Новое Имя");
        assertNotNull(result, "editName() не должен возвращать null");
        assertSame(profilePage, result, "editName() должен возвращать this для Fluent API");
    }
}

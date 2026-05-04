package com.example.runner.harness.module3.block3;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.clickable;
import static org.junit.jupiter.api.Assertions.*;

public class AdminPageHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/home";
    }

    @Test
    void testAdminBasePageExtendsBasePage() throws Exception {
        Class<?> adminBaseClass = loadStudentClass("com.example.runner.pom.pages.AdminBasePage");
        Class<?> basePageClass = loadStudentClass("com.example.runner.pom.pages.BasePage");

        assertTrue(basePageClass.isAssignableFrom(adminBaseClass),
                "AdminBasePage должен наследоваться от BasePage");
    }

    @Test
    void testAdminUsersPageExtendsAdminBasePage() throws Exception {
        Class<?> adminUsersClass = loadStudentClass("com.example.runner.pom.pages.AdminUsersPage");
        Class<?> adminBaseClass = loadStudentClass("com.example.runner.pom.pages.AdminBasePage");

        assertTrue(adminBaseClass.isAssignableFrom(adminUsersClass),
                "AdminUsersPage должен наследоваться от AdminBasePage");
    }

    @Test
    void testOpenSidebar() throws Exception {
        $("#sidebar-menu").shouldBe(clickable, java.time.Duration.ofSeconds(5));

        Class<?> adminBaseClass = loadStudentClass("com.example.runner.pom.pages.AdminBasePage");
        Object adminPage = adminBaseClass.getDeclaredConstructor(String.class).newInstance("//div[@id='home-page']");

        Object result = invoke(adminPage, "openSidebar");

        assertNotNull(result, "openSidebar() не должен возвращать null");
        assertSame(adminPage, result, "openSidebar() должен возвращать this");
    }

    @Test
    void testGoToUserManagement() throws Exception {
        $("#user-management").shouldBe(clickable, java.time.Duration.ofSeconds(5));

        Class<?> adminBaseClass = loadStudentClass("com.example.runner.pom.pages.AdminBasePage");
        Object adminPage = adminBaseClass.getDeclaredConstructor(String.class).newInstance("//div[@id='home-page']");

        Object result = invoke(adminPage, "goToUserManagement");

        assertNotNull(result, "goToUserManagement() не должен возвращать null");
        assertSame(adminPage, result, "goToUserManagement() должен возвращать this");
    }

    @Test
    void testGetPageTitle() throws Exception {
        Class<?> adminUsersClass = loadStudentClass("com.example.runner.pom.pages.AdminUsersPage");
        Object adminUsersPage = adminUsersClass.getDeclaredConstructor().newInstance();

        Object result = invoke(adminUsersPage, "getPageTitle");

        assertNotNull(result, "getPageTitle() не должен возвращать null");
        assertInstanceOf(String.class, result, "getPageTitle() должен возвращать String");
    }
}

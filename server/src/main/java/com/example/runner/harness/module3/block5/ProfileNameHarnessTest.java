package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест имени в профиле */
public class ProfileNameHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/profile";
    }

    private Object createProfilePage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testEditNameExists() throws Exception {
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        java.lang.reflect.Method method = profilePageClass.getDeclaredMethod("editName", String.class);
        assertNotNull(method, "ProfilePage должен содержать метод editName(String)");
        assertEquals(profilePageClass, method.getReturnType(),
                "editName() должен возвращать ProfilePage (Fluent API)");
    }

    @Test
    void testSaveChangesExists() throws Exception {
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        java.lang.reflect.Method method = profilePageClass.getDeclaredMethod("saveChanges");
        assertNotNull(method, "ProfilePage должен содержать метод saveChanges()");
    }

    @Test
    void testGetDisplayNameReturnsString() throws Exception {
        Object profilePage = createProfilePage();

        Object result = invoke(profilePage, "getDisplayName");
        assertNotNull(result, "getDisplayName() не должен возвращать null");
        assertInstanceOf(String.class, result, "getDisplayName() должен возвращать String");
    }

    @Test
    void testEditAndSaveRealBehavior() throws Exception {
        Object profilePage = createProfilePage();

        invoke(profilePage, "editName", "Новое Имя");

        $("#name").shouldBe(visible, java.time.Duration.ofSeconds(3));
        assertEquals("Новое Имя", $("#name").getValue(),
                "editName() должен заполнить поле ввода новым именем");

        invoke(profilePage, "saveChanges");

        $("#profile-name-display").shouldHave(text("Новое Имя"), java.time.Duration.ofSeconds(3));
    }

    @Test
    void testGetDisplayNameRealBehavior() throws Exception {
        Object profilePage = createProfilePage();

        invoke(profilePage, "editName", "Тестовый Пользователь");
        invoke(profilePage, "saveChanges");

        Object result = invoke(profilePage, "getDisplayName");
        assertNotNull(result);
        assertEquals("Тестовый Пользователь", result,
                "getDisplayName() должен вернуть сохранённое имя");
    }

}

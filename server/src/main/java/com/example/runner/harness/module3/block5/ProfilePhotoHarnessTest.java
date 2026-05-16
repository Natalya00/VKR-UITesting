package com.example.runner.harness.module3.block5;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Condition.visible;
import static org.junit.jupiter.api.Assertions.*;

/** Harness-тест загрузки фото профиля */
public class ProfilePhotoHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/profile";
    }

    private Object createProfilePage() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        return cls.getDeclaredConstructor().newInstance();
    }

    @Test
    void testUploadPhotoExists() throws Exception {
        Class<?> profilePageClass = loadStudentClass("com.example.runner.pom.pages.ProfilePage");
        java.lang.reflect.Method method = profilePageClass.getDeclaredMethod("uploadPhoto", java.io.File.class);
        assertNotNull(method, "ProfilePage должен содержать метод uploadPhoto(File)");
    }

    @Test
    void testIsPhotoUploadedReturnsBoolean() throws Exception {
        Object profilePage = createProfilePage();
        Object result = invoke(profilePage, "isPhotoUploaded");
        assertNotNull(result, "isPhotoUploaded() не должен возвращать null");
        assertInstanceOf(Boolean.class, result, "isPhotoUploaded() должен возвращать boolean");
    }

    @Test
    void testPhotoUploadIndicatorExists() throws Exception {
        $x("//input[@type='file']").shouldBe(visible);
    }

    @Test
    void testUploadButtonExists() throws Exception {
        $("[data-testid='upload-photo']").shouldBe(visible);
    }

    @Test
    void testUploadPhotoRealBehavior() throws Exception {
        Path tempFile = Files.createTempFile("test-photo", ".jpg");
        Files.write(tempFile, new byte[]{1, 2, 3, 4});

        try {
            Object profilePage = createProfilePage();

            assertDoesNotThrow(() -> invoke(profilePage, "uploadPhoto", tempFile.toFile()),
                    "uploadPhoto() не должен падать");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testUploadAndCheckPhoto() throws Exception {
        Path tempFile = Files.createTempFile("test-photo", ".jpg");
        Files.write(tempFile, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        try {
            Object profilePage = createProfilePage();

            invoke(profilePage, "uploadPhoto", tempFile.toFile());

            Object result = invoke(profilePage, "isPhotoUploaded");
            assertNotNull(result, "isPhotoUploaded() не должен возвращать null");
            assertTrue((Boolean) result, "isPhotoUploaded() должен вернуть true после загрузки фото");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

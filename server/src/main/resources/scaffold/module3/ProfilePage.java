package com.example.runner.pom.pages;
import java.io.File;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Страница профиля пользователя с возможностью загрузки фотографии
 */
public class ProfilePage extends BasePage {

    /**
     * Инициализирует страницу профиля с корневым XPath-локатором
     */
    public ProfilePage() {
        super("//div[@id='profile-page']");
    }

    /**
     * Загружает фотографию профиля через поле выбора файла
     * @param file файл изображения для загрузки
     * @return текущий экземпляр страницы для цепочки вызовов
     */
    public ProfilePage uploadPhoto(File file) {
        $x("//input[@type='file']").uploadFile(file);
        return this;
    }

    /**
     * Проверяет, отображается ли сообщение об успешной загрузке фотографии
     * @return true, если загрузка прошла успешно и отображается уведомление
     */
    public boolean isPhotoUploaded() {
        return $x("//div[@data-testid='photo-upload-success']").isDisplayed();
    }
}

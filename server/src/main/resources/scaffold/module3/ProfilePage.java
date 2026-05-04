package com.example.runner.pom.pages;
import java.io.File;

import static com.codeborne.selenide.Selenide.$x;

public class ProfilePage extends BasePage {

    public ProfilePage() {
        super("//div[@id='profile-page']");
    }

    public ProfilePage uploadPhoto(File file) {
        $x("//input[@type='file']").uploadFile(file);
        return this;
    }

    public boolean isPhotoUploaded() {
        return $x("//div[@data-testid='photo-upload-success']").isDisplayed();
    }
}

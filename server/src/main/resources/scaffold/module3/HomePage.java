package com.example.runner.pom.pages;
import static com.codeborne.selenide.Selenide.$x;

/** Главная страница приложения */
public class HomePage extends BasePage {

    public HomePage() {
        super("//div[@id='home-page']");
    }

    public boolean checkPageLoaded() {
        return basePage.isDisplayed();
    }

    public LoginPage logout() {
        $x("//button[@id='logout-btn']").click();
        return new LoginPage();
    }

    public ProfilePage goToProfile() {
        $x("//a[@href='/profile']").click();
        return new ProfilePage();
    }
}

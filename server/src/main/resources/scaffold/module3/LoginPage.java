package com.example.runner.pom.pages;

import com.example.runner.pom.elements.Input;
import com.example.runner.pom.elements.Button;

import static com.codeborne.selenide.Selenide.$x;

public class LoginPage extends BasePage {

    private Input loginInput = Input.byId("login-input");
    private Input passwordInput = Input.byId("password-input");
    private Button submitButton = new Button("//button[@id='submit-btn']", "");

    public LoginPage() {
        super("//form[@id='login-form']");
    }

    public LoginPage fillLogin(String login) {
        loginInput.fill(login);
        return this;
    }

    public LoginPage fillPassword(String password) {
        passwordInput.fill(password);
        return this;
    }

    public HomePage clickLoginButton() {
        submitButton.click();
        return new HomePage();
    }

    public String getErrorMessage() {
        return $x("//div[@data-testid='error-message']").getText();
    }

    public boolean isDisplayed() {
        return basePage.isDisplayed();
    }
}

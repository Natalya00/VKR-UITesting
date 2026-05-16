package com.example.runner.services;

import com.example.runner.tests.User;
import com.example.runner.pom.pages.LoginPage;
import com.example.runner.pom.pages.HomePage;

/** Сервис для выполнения операций аутентификации */
public class AuthService {

    public HomePage login(User user) {
        LoginPage loginPage = new LoginPage();
        loginPage.fillLogin(user.getLogin());
        loginPage.fillPassword(user.getPassword());
        return loginPage.clickLoginButton();
    }
}

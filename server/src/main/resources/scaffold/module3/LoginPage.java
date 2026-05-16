package com.example.runner.pom.pages;

import com.example.runner.pom.elements.Input;
import com.example.runner.pom.elements.Button;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Страница входа в приложение с полями логина и пароля
 */
public class LoginPage extends BasePage {

    /** Поле ввода логина */
    private Input loginInput = Input.byId("login-input");
    /** Поле ввода пароля */
    private Input passwordInput = Input.byId("password-input");
    /** Кнопка отправки формы входа */
    private Button submitButton = new Button("//button[@id='submit-btn']", "");

    /**
     * Инициализирует страницу входа с корневым XPath-локатором формы
     */
    public LoginPage() {
        super("//form[@id='login-form']");
    }

    /**
     * Заполняет поле логина указанным значением
     * @param login логин пользователя
     * @return текущий экземпляр страницы для цепочки вызовов
     */
    public LoginPage fillLogin(String login) {
        loginInput.fill(login);
        return this;
    }

    /**
     * Заполняет поле пароля указанным значением
     * @param password пароль пользователя
     * @return текущий экземпляр страницы для цепочки вызовов
     */
    public LoginPage fillPassword(String password) {
        passwordInput.fill(password);
        return this;
    }

    /**
     * Нажимает кнопку входа и переходит на главную страницу
     * @return экземпляр главной страницы после успешного входа
     */
    public HomePage clickLoginButton() {
        submitButton.click();
        return new HomePage();
    }

    /**
     * Возвращает текст сообщения об ошибке аутентификации
     * @return текст ошибки, отображаемый на странице
     */
    public String getErrorMessage() {
        return $x("//div[@data-testid='error-message']").getText();
    }

    /**
     * Проверяет, отображается ли форма входа на странице
     * @return true, если форма входа видима
     */
    public boolean isDisplayed() {
        return basePage.isDisplayed();
    }
}

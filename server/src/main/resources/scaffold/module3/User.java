package com.example.runner.tests;

/**
 * Модель тестового пользователя с учётными данными для аутентификации
 */
public class User {
    /** Логин пользователя */
    private String login;
    /** Пароль пользователя */
    private String password;

    /**
     * Создаёт пользователя с указанными учётными данными
     * @param login логин пользователя
     * @param password пароль пользователя
     */
    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * Возвращает логин пользователя
     * @return логин пользователя
     */
    public String getLogin() {
        return login;
    }

    /**
     * Возвращает пароль пользователя
     * @return пароль пользователя
     */
    public String getPassword() {
        return password;
    }
}

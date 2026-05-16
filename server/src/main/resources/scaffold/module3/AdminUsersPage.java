package com.example.runner.pom.pages;

/** Страница управления пользователями в админ-панели */
public class AdminUsersPage extends AdminBasePage {
    
    public AdminUsersPage() {
        super("//div[@id='admin-users-page']");
    }
    
    public int getUserCount() {
        return 0;
    }
}

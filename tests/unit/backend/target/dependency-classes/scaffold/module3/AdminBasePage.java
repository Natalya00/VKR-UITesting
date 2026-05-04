package com.example.runner.pom.pages;

import com.example.runner.pom.elements.Button;


public class AdminBasePage extends BasePage {
    
    protected Button adminMenu;
    protected Button userManagementButton;
    
    public AdminBasePage(String xpath) {
        super(xpath);
        this.adminMenu = Button.byId("admin-menu");
        this.userManagementButton = Button.byId("user-management");
    }
    
    public AdminBasePage openUserManagement() {
        userManagementButton.click();
        return this;
    }
}

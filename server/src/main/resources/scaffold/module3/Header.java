package com.example.runner.pom.components;
import com.codeborne.selenide.SelenideElement;

/** Компонент заголовка страницы с навигацией */
public class Header {

    protected SelenideElement baseElement;

    public Header(SelenideElement baseElement) {
        this.baseElement = baseElement;
    }

    public Header clickLogo() {
        baseElement.$x(".//img[@alt='Логотип']").click();
        return this;
    }

    public Header clickProfile() {
        baseElement.$x(".//a[contains(@href, '/profile')]").click();
        return this;
    }

    public Header clickNavigation(String link) {
        baseElement.$x(".//a[text()='" + link + "']").click();
        return this;
    }
}

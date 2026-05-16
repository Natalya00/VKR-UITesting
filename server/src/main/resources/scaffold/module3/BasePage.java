package com.example.runner.pom.pages;

import com.codeborne.selenide.SelenideElement;
import com.example.runner.pom.components.Header;
import com.example.runner.pom.components.Footer;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.page;

/** Базовый класс для всех страниц с общими элементами */
public class BasePage {

    protected SelenideElement basePage;

    protected Header header;

    protected Footer footer;

    protected BasePage(String xpath) {
        this.basePage = $x(xpath);
        this.header = new Header($x("//header"));
        this.footer = new Footer($x("//footer"));
    }

    protected SelenideElement $x(String xpath) {
        return com.codeborne.selenide.Selenide.$x(xpath);
    }

    public BasePage refresh() {
        refresh();
        return this;
    }

    public boolean isDisplayed() {
        return basePage.isDisplayed();
    }

    public <T> T page(Class<T> pageClass) {
        return page(pageClass);
    }
}

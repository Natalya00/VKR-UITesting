package com.example.runner.pom.elements;

import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Condition.visible;

/** Базовый класс для всех элементов страницы */
public class BaseElement {

    protected SelenideElement baseElement;

    protected static final int WAIT_SECONDS = 5;

    protected BaseElement(String xpath, String attributeValue) {
        this.baseElement = $x(String.format(xpath, attributeValue));
    }

    public boolean isDisplayed() {
        try {
            baseElement.shouldBe(visible, java.time.Duration.ofSeconds(WAIT_SECONDS));
            return true;
        } catch (com.codeborne.selenide.ex.ElementNotFound |
                 com.codeborne.selenide.ex.ElementShould |
                 org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void click() {
        baseElement.click();
    }

    protected SelenideElement findInside(String relativeXpath) {
        return baseElement.$x(relativeXpath);
    }
}
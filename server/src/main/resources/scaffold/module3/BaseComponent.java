package com.example.runner.pom.components;

import com.codeborne.selenide.SelenideElement;

/** Базовый класс для компонентов страницы */
public class BaseComponent {
    protected SelenideElement baseElement;

    public BaseComponent(SelenideElement baseElement) {
        this.baseElement = baseElement;
    }
}

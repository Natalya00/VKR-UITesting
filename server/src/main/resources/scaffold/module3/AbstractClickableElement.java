package com.example.runner.pom.elements;

/** Абстрактный базовый класс для кликабельных элементов */
public abstract class AbstractClickableElement extends BaseElement implements Clickable {
    
    protected AbstractClickableElement(String xpath, String attributeValue) {
        super(xpath, attributeValue);
    }
    
    @Override
    public void click() {
        baseElement.click();
    }
}

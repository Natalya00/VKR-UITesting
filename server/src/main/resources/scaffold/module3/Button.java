package com.example.runner.pom.elements;

/** Элемент кнопки с методами поиска по ID */
public class Button extends AbstractClickableElement {
    public Button(String xpath, String value) {
        super(xpath, value);
    }

    public static Button byId(String id) {
        return new Button("//*[@id='%s']", id);
    }
}

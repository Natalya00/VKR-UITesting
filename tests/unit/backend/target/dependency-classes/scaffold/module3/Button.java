package com.example.runner.pom.elements;

public class Button extends AbstractClickableElement {
    public Button(String xpath, String value) {
        super(xpath, value);
    }

    public static Button byId(String id) {
        return new Button("//*[@id='%s']", id);
    }
}

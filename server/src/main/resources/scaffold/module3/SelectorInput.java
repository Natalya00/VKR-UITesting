package com.example.runner.pom.elements;

public class SelectorInput extends Input {
    
    private SelectorInput(String xpath, String value) {
        super(xpath, value);
    }
    
    public SelectorInput select(String value) {
        baseElement.click();
        baseElement.$x(String.format("//option[text()='%s']", value)).click();
        return this;
    }
    
    public static SelectorInput byId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID не может быть null или пустым");
        }
        return new SelectorInput("//*[@id='%s']", id);
    }
    
    public static SelectorInput byName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        return new SelectorInput("//*[@name='%s']", name);
    }
}

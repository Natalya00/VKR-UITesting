package com.example.runner.pom.elements;

/** Элемент поля ввода с методами заполнения и поиска */
public class Input extends BaseElement {
    private static final String ID_XPATH = "//*[@id='%s']";
    private static final String NAME_XPATH = "//*[@name='%s']";
    private static final String PLACEHOLDER_XPATH = "//*[@placeholder='%s']";
    
    protected Input(String xpath, String value) {
        super(xpath, value);
    }
    
    public Input fill(String value) {
        baseElement.clear();
        baseElement.setValue(value);
        return this;
    }
    
    public static Input byId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID не может быть null или пустым");
        }
        return new Input(ID_XPATH, id);
    }
    
    public static Input byName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        return new Input(NAME_XPATH, name);
    }
    
    public static Input byPlaceholder(String placeholder) {
        if (placeholder == null || placeholder.isEmpty()) {
            throw new IllegalArgumentException("Placeholder не может быть null или пустым");
        }
        return new Input(PLACEHOLDER_XPATH, placeholder);
    }
}

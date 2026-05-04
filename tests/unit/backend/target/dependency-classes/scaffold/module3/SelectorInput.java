package com.example.runner.pom.elements;

/**
 * Элемент SelectorInput для работы с выпадающими списками.
 */
public class SelectorInput extends Input {
    
    /**
     * Приватный конструктор.
     * @param xpath XPath-шаблон
     * @param value Значение для подстановки
     */
    private SelectorInput(String xpath, String value) {
        super(xpath, value);
    }
    
    /**
     * Выбрать значение из выпадающего списка.
     * @param value Значение для выбора
     * @return текущий объект для fluent-вызовов
     */
    public SelectorInput select(String value) {
        // Реальное пользовательское поведение: клик по полю, выбор значения
        baseElement.click();
        baseElement.$x(String.format("//option[text()='%s']", value)).click();
        return this;
    }
    
    /**
     * Создать SelectorInput по ID.
     * @param id Значение ID атрибута
     * @return Объект SelectorInput
     * @throws IllegalArgumentException если id null или пустой
     */
    public static SelectorInput byId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID не может быть null или пустым");
        }
        return new SelectorInput("//*[@id='%s']", id);
    }
    
    /**
     * Создать SelectorInput по name.
     * @param name Значение name атрибута
     * @return Объект SelectorInput
     * @throws IllegalArgumentException если name null или пустой
     */
    public static SelectorInput byName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        return new SelectorInput("//*[@name='%s']", name);
    }
}

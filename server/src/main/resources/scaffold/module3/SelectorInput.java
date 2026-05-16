package com.example.runner.pom.elements;

/**
 * Элемент выпадающего списка (select) с методами выбора значения и поиска
 */
public class SelectorInput extends Input {
    
    /**
     * Создаёт элемент select по XPath-шаблону и значению атрибута
     * @param xpath XPath-шаблон с плейсхолдером %s
     * @param value значение атрибута для подстановки в шаблон
     */
    private SelectorInput(String xpath, String value) {
        super(xpath, value);
    }
    
    /**
     * Выбирает опцию в выпадающем списке по отображаемому тексту
     * @param value текст опции для выбора
     * @return текущий экземпляр элемента для цепочки вызовов
     */
    public SelectorInput select(String value) {
        baseElement.click();
        baseElement.$x(String.format("//option[text()='%s']", value)).click();
        return this;
    }
    
    /**
     * Создаёт элемент select по атрибуту id
     * @param id значение атрибута id элемента
     * @return экземпляр SelectorInput для указанного id
     * @throws IllegalArgumentException если id равен null или пустой строке
     */
    public static SelectorInput byId(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID не может быть null или пустым");
        }
        return new SelectorInput("//*[@id='%s']", id);
    }
    
    /**
     * Создаёт элемент select по атрибуту name
     * @param name значение атрибута name элемента
     * @return экземпляр SelectorInput для указанного name
     * @throws IllegalArgumentException если name равен null или пустой строке
     */
    public static SelectorInput byName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name не может быть null или пустым");
        }
        return new SelectorInput("//*[@name='%s']", name);
    }
}

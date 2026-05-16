package com.example.runner.pom.components;
import com.codeborne.selenide.SelenideElement;

/**
 * Строка таблицы на странице админ-панели
 */
public class TableRow {
    /** Корневой элемент строки таблицы */
    private SelenideElement baseElement;

    /**
     * Создаёт обёртку над элементом строки таблицы
     * @param baseElement Selenide-элемент, представляющий строку таблицы
     */
    public TableRow(SelenideElement baseElement) {
        this.baseElement = baseElement;
    }

    /**
     * Возвращает текстовое содержимое строки таблицы
     * @return текст строки
     */
    public String getText() {
        return baseElement.getText();
    }
}

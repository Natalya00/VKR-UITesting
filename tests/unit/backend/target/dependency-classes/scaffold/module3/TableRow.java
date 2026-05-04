package com.example.runner.pom.components;
import com.codeborne.selenide.SelenideElement;

public class TableRow {
    private SelenideElement baseElement;

    public TableRow(SelenideElement baseElement) {
        this.baseElement = baseElement;
    }

    public String getText() {
        return baseElement.getText();
    }
}

package com.example.runner.pom.elements;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Condition.visible;

/** Компонент отдельного элемента в списке */
public class ItemComponent {
    protected SelenideElement baseElement;

    public ItemComponent(SelenideElement element) {
        this.baseElement = element;
    }

    public String getTitle() {
        return baseElement.$x(".//h3").getText().trim();
    }

    public void click() {
        baseElement.click();
    }

    public boolean isDisplayed() {
        return baseElement.isDisplayed();
    }

    public void clickActionButton(String buttonText) {
        baseElement.$x(".//button[text()='" + buttonText + "']").click();
    }
}

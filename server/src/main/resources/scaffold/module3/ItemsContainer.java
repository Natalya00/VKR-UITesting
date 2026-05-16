package com.example.runner.pom.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.exist;

/** Контейнер для работы с коллекцией элементов */
public class ItemsContainer extends BaseElement {
    public ItemsContainer(String xpath, String attributeValue) {
        super(xpath, attributeValue);
    }

    public List<ItemComponent> getItems() {
        List<ItemComponent> result = new ArrayList<>();
        baseElement.$("div.item-card").should(exist, java.time.Duration.ofSeconds(5));
        
        ElementsCollection items = baseElement.$$("div.item-card");
        for (SelenideElement itemElement : items) {
            result.add(new ItemComponent(itemElement));
        }
        return result;
    }

    public ItemComponent getItemByTitle(String title) {
        List<ItemComponent> items = getItems();
        for (ItemComponent item : items) {
            String itemTitle = item.getTitle();
            if (itemTitle != null && itemTitle.equals(title)) {
                return item;
            }
        }
        return null;
    }

    public boolean hasItem(String title) {
        return getItemByTitle(title) != null;
    }
}

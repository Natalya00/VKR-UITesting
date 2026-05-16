package com.example.runner.pom.pages;

/** Страница каталога продуктов приложения */
public class ProductPage extends BasePage {

    /** Инициализирует страницу продуктов с корневым XPath-локатором */
    public ProductPage() {
        super("//div[@id='products-page']");
    }
}

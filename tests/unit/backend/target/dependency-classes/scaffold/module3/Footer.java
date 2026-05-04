package com.example.runner.pom.components;

import com.codeborne.selenide.SelenideElement;

public class Footer {

    protected SelenideElement baseElement;

    public Footer(SelenideElement baseElement) {
        this.baseElement = baseElement;
    }

    public Footer clickSocialLink(String network) {
        baseElement.$x(".//a[contains(@href, '" + network + "')]").click();
        return this;
    }

    public String getCopyrightText() {
        return baseElement.$x(".//p[contains(@class, 'copyright')]").getText();
    }
}

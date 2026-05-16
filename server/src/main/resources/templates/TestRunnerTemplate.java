package com.example.runner.service;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.WebDriverRunner;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Шаблон Selenide-раннера для динамической проверки упражнений модуля 2
 */
public class TestRunnerTemplate {

    /**
     * Точка входа: настройка Chrome, открытие страницы упражнения, инъекция скрипта и валидация
     * @param args {@code [baseUrl]} или {@code [baseUrl, exerciseNum]}
     */
    public static void main(String[] args) throws Exception {
        Selenide.closeWebDriver();

        String baseUrl;
        if (args.length > 0 && !"http://localhost:5173".equals(args[0])) {
            baseUrl = args[0];
        } else {
            baseUrl = resolveBaseUrl();
        }
        System.setProperty("webdriver.http.factory", "jdk-http-client");

        Configuration.browser = "chrome";
        Configuration.headless = true;
        Configuration.reopenBrowserOnFail = false;
        Configuration.browserSize = "1280x720";
        Configuration.timeout = 30000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.baseUrl = baseUrl;

        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.SEVERE);

        System.out.println("[TestRunner] Chrome config...");
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--headless=new",
            "--disable-web-security",
            "--disable-extensions"
        );
        Configuration.browserCapabilities = options;

        boolean success = false;
        int exerciseNum = 1;
        if (args.length > 1) {
            try {
                exerciseNum = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            String url = baseUrl + "/element-simulator?exercise=" + exerciseNum;
            
            System.out.println("[TestRunner] Загрузка страницы: " + url);
            System.out.println("[TestRunner] Упражнение №" + exerciseNum + ", baseUrl: " + baseUrl);

            open(url);
            
            $("body").shouldBe(visible, Duration.ofSeconds(10));
            
            Thread.sleep(1000);
            
            String loadSelector = getLoadSelector(exerciseNum);

            if (loadSelector != null) {
                waitForElement(loadSelector, 30);
                System.out.println("[TestRunner] Элемент найден: " + loadSelector);
            }

            injectUiTrackers();
            saveInitialState();

            Thread.sleep(500);

            /* __INJECT_USER_SCRIPT__ */

            success = validateExercise(exerciseNum);
        } catch (Throwable e) {
            System.err.println("[TestRunner] ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            try {
                String screenshotPath = "error_screenshot_exercise_" + exerciseNum + "_" + System.currentTimeMillis() + ".png";
                Selenide.screenshot(screenshotPath);
                System.err.println("[TestRunner] Скриншот сохранен: " + screenshotPath);
            } catch (Exception screenshotError) {
                System.err.println("[TestRunner] Не удалось сделать скриншот: " + screenshotError.getMessage());
            }
            
            success = false;
        } finally {
            Selenide.closeWebDriver();
        }

        if (success) {
            System.out.println("[TestRunner] Динамическая проверка: УСПЕХ");
        } else {
            System.out.println("[TestRunner] Динамическая проверка: НЕ УСПЕХ");
        }
        System.exit(success ? 0 : 1);
    }

    /** @param exerciseNum номер упражнения; @return CSS-селектор ожидаемого элемента или null */
    private static String getLoadSelector(int exerciseNum) {
        return switch (exerciseNum) {
            case 1, 16 -> "#submit-btn";
            case 2 -> "a[href='#about']";
            case 3 -> "#menu-item";
            case 4 -> "#cancel-btn";
            case 5 -> ".nav-item[data-page='home']";
            case 6 -> "#username";
            case 7, 11 -> "#notes";
            case 8 -> "#search";
            case 9 -> "#search-input";
            case 10 -> "#user-email";
            case 12 -> "#welcome-message";
            case 13 -> "#status";
            case 14 -> ".error-message";
            case 15 -> ".loader";
            case 17 -> "#user-email";
            case 18 -> ".notification";
            case 19 -> "#age";
            case 20 -> "#content";
            case 21 -> null;
            case 22 -> ".items li";
            case 23 -> ".nav";
            case 24 -> "#users";
            case 25 -> ".error";
            case 26 -> ".task";
            case 27 -> ".btn";
            case 28 -> "input[type='checkbox']";
            case 29 -> ".role";
            case 30 -> ".msg";
            case 31 -> ".task";
            case 32 -> ".items li";
            case 33 -> "#dropdown-city";
            case 34 -> "#dropdown-country";
            case 35 -> "#dropdown";
            case 36 -> "#dropdown-lang";
            case 37 -> "#dropdown-multi";
            case 38 -> "#dropdown-country";
            case 39 -> "#dropdown";
            // Блок 5
            case 40 -> "#agree-checkbox";
            case 41 -> "#newsletter-checkbox";
            case 42 -> "#terms-checkbox";
            case 43 -> "#optional-checkbox";
            case 44 -> "#payment-card";
            case 45 -> "input[name='payment'][value='cash']";
            case 46 -> "label";
            case 47 -> ".opt";
            case 48 -> "#toggle-checkbox";
            case 49 -> "#readonly-checkbox";
            case 50 -> "#option-b";
            case 51 -> ".preference";
            // Блок 6
            case 52 -> "#menu-item";
            case 53 -> "#editable-text";
            case 54 -> "#area";
            case 55 -> "#source";
            case 56 -> ".file";
            case 57 -> "#tooltip";
            case 58 -> "#slider-handle";
            case 59 -> "#resizable";
            // Блок 7
            case 60 -> "#alert-btn";
            case 61 -> "#alert-text-btn";
            case 62 -> "#delete-btn";
            case 63 -> "#prompt-btn";
            case 64 -> "#start-btn";
            case 65 -> "#prompt-dismiss-btn";
            case 66 -> "#confirm-btn";
            case 67 -> "#alert-dismiss-btn";
            case 68 -> "#submit-btn";
            // Блок 7.5: HTML-модальные окна
            case 69 -> "#open-modal-btn";
            case 70 -> "#open-esc-btn";
            case 71 -> "#open-close-btn";
            case 72 -> "#open-overlay-btn";
            case 73 -> "#open-form-btn";
            case 74 -> "#open-cascade-btn";
            // Блок 8
            case 75 -> "#payment-frame";
            case 76 -> "#content-frame";
            case 77 -> "#outer-frame";
            case 78 -> "#main-frame";
            case 79 -> "#outer-frame";
            case 80 -> "#dynamic-frame";
            default -> "body";
        };
    }

    /** Ожидает появления элемента на странице с таймаутом */
    private static void waitForElement(String selector, long timeoutSec) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutSec * 1000) {
            try {
                SelenideElement el = $(selector);
                if (el.exists() && el.isDisplayed()) {
                    return;
                }
            } catch (Exception ignored) {}
            Thread.sleep(500);
        }
        
        try {
            String screenshotPath = "timeout_screenshot_" + selector.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".png";
            Selenide.screenshot(screenshotPath);
            System.err.println("[TestRunner] Скриншот таймаута сохранен: " + screenshotPath);
        } catch (Exception screenshotError) {
            System.err.println("[TestRunner] Не удалось сделать скриншот таймаута: " + screenshotError.getMessage());
        }
        
        throw new RuntimeException("Element not found or not visible after " + timeoutSec + "s: " + selector);
    }

    /** Внедряет в страницу JS-трекеры событий UI */
    private static void injectUiTrackers() {
        executeJavaScript(
            "window.__uiState = { " +
            "  submitClicked: false, linkTexts: [], hoveredMenu: false, " +
            "  cancelClicked: false, cancelVisibleAtClick: false, cancelEnabledAtClick: false, " +
            "  navClicks: [], targetClicked: false, firstBtnClicked: false, " +
            "  contextMenuShown: false, dragDropCompleted: false, elementEdited: false, " +
            "  tooltipVisible: false, sliderMoved: false, elementResized: false " +
            "}; " +
            // Блок 5: Отслеживание чекбоксов и радиокнопок
            "document.querySelectorAll('input[type=\"checkbox\"]').forEach(el => { " +
            "  el.addEventListener('change', () => { " +
            "    el.setAttribute('data-checked', el.checked ? 'true' : 'false'); " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('input[type=\"radio\"]').forEach(el => { " +
            "  el.addEventListener('change', () => { " +
            "    el.setAttribute('data-selected', el.checked ? 'true' : 'false'); " +
            "  }); " +
            "}); " +
            // Блок 1-4: Отслеживание кликов
            "document.querySelectorAll('button, a, .nav-item').forEach(el => { " +
            "  ['click', 'mouseover'].forEach(event => { " +
            "    el.addEventListener(event, e => { " +
            "      let text = el.textContent?.trim() || ''; " +
            "      if (event === 'click') { " +
            "        window.__uiState.linkTexts.push(text); " +
            "        window.__uiState.navClicks.push(el.dataset.page || text); " +
            "        if (el.id === 'submit-btn') window.__uiState.submitClicked = true; " +
            "        if (el.id === 'cancel-btn') { " +
            "          window.__uiState.cancelClicked = true; " +
            "          window.__uiState.cancelVisibleAtClick = !!document.getElementById('cancel-btn')?.offsetParent; " +
            "          window.__uiState.cancelEnabledAtClick = !document.getElementById('cancel-btn')?.disabled; " +
            "        } " +
            "        if (el.classList.contains('btn') && el.offsetParent !== null) { " +
            "          window.__uiState.firstBtnClicked = true; " +
            "        } " +
            "      } else if (el.id === 'menu-item') { " +
            "        window.__uiState.hoveredMenu = true; " +
            "      } " +
            "    }); " +
            "  }); " +
            "});" +
            // Блок 6: Отслеживание сложных действий
            "document.querySelectorAll('#menu-item').forEach(el => { " +
            "  el.addEventListener('mouseover', () => { el.setAttribute('data-hovered', 'true'); }); " +
            "}); " +
            "document.querySelectorAll('#editable-text').forEach(el => { " +
            "  el.addEventListener('dblclick', () => { " +
            "    el.contentEditable = 'true'; " +
            "    el.focus(); " +
            "    el.setAttribute('data-edited', 'true'); " +
            "    window.__uiState.elementEdited = true; " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#area').forEach(el => { " +
            "  el.addEventListener('contextmenu', (e) => { " +
            "    e.preventDefault(); " +
            "    el.setAttribute('data-context-clicked', 'true'); " +
            "    window.__uiState.contextMenuShown = true; " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#source').forEach(el => { " +
            "  el.setAttribute('draggable', 'true'); " +
            "  el.addEventListener('dragend', () => { " +
            "    el.setAttribute('data-dropped', 'true'); " +
            "    window.__uiState.dragDropCompleted = true; " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#target').forEach(el => { " +
            "  el.addEventListener('dragover', (e) => { e.preventDefault(); }); " +
            "  el.addEventListener('drop', (e) => { " +
            "    e.preventDefault(); " +
            "    el.setAttribute('data-dropped-on', 'true'); " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('.file').forEach(el => { " +
            "  el.addEventListener('click', (e) => { " +
            "    if (e.ctrlKey || e.metaKey) { " +
            "      el.classList.toggle('selected'); " +
            "      el.setAttribute('data-selected', el.classList.contains('selected') ? 'true' : 'false'); " +
            "    } " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#tooltip').forEach(el => { " +
            "  el.addEventListener('mouseenter', () => { " +
            "    el.setAttribute('data-tooltip-visible', 'true'); " +
            "    window.__uiState.tooltipVisible = true; " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#slider-handle').forEach(el => { " +
            "  let startX = 0; " +
            "  el.addEventListener('mousedown', (e) => { " +
            "    startX = e.clientX; " +
            "    const onMouseMove = (moveEvent) => { " +
            "      const delta = moveEvent.clientX - startX; " +
            "      el.setAttribute('data-position', Math.abs(delta)); " +
            "      el.style.transform = 'translateX(' + delta + 'px)'; " +
            "      if (Math.abs(delta) >= 100) window.__uiState.sliderMoved = true; " +
            "    }; " +
            "    const onMouseUp = () => { " +
            "      document.removeEventListener('mousemove', onMouseMove); " +
            "      document.removeEventListener('mouseup', onMouseUp); " +
            "    }; " +
            "    document.addEventListener('mousemove', onMouseMove); " +
            "    document.addEventListener('mouseup', onMouseUp); " +
            "  }); " +
            "}); " +
            "document.querySelectorAll('#resizable').forEach(el => { " +
            "  let startY = 0; " +
            "  let startHeight = 0; " +
            "  el.setAttribute('data-height-before', el.offsetHeight); " +
            "  el.addEventListener('mousedown', (e) => { " +
            "    startY = e.clientY; " +
            "    startHeight = el.offsetHeight; " +
            "    const onMouseMove = (moveEvent) => { " +
            "      const newHeight = startHeight + (moveEvent.clientY - startY); " +
            "      el.style.height = newHeight + 'px'; " +
            "      el.setAttribute('data-height-after', newHeight); " +
            "      if (newHeight - startHeight >= 30) window.__uiState.elementResized = true; " +
            "    }; " +
            "    const onMouseUp = () => { " +
            "      document.removeEventListener('mousemove', onMouseMove); " +
            "      document.removeEventListener('mouseup', onMouseUp); " +
            "    }; " +
            "    document.addEventListener('mousemove', onMouseMove); " +
            "    document.addEventListener('mouseup', onMouseUp); " +
            "  }); " +
            "});"
        );
    }

    /** Сохраняет начальное состояние полей и элементов для последующего сравнения */
    private static void saveInitialState() {
        executeJavaScript(
            "window.__initialState = {values:{}, texts:{}, visible:{}, disabled:{}, classes:{}, checked:{}}; " +
            "document.querySelectorAll('input, textarea').forEach(el => { " +
            "  let id = el.id || el.className; " +
            "  if (id) { " +
            "    window.__initialState.values[id] = el.value; " +
            "    window.__initialState.checked[id] = el.checked; " +
            "  } " +
            "}); " +
            "document.querySelectorAll('[id]').forEach(el => { " +
            "  window.__initialState.texts[el.id] = el.innerText; " +
            "  window.__initialState.visible[el.id] = !!el.offsetParent; " +
            "  window.__initialState.classes[el.id] = el.className; " +
            "}); " +
            "document.querySelectorAll('button, input, select').forEach(el => { " +
            "  let id = el.id || el.className; if (id) window.__initialState.disabled[id] = el.disabled; " +
            "});"
        );
    }

    /**
     * Проверяет результат выполнения скрипта для заданного упражнения
     * @param exerciseNum номер упражнения (1–N)
     * @return true, если проверки пройдены
     */
    private static boolean validateExercise(int exerciseNum) {
        System.out.println("[TestRunner] Начало валидации упражнения " + exerciseNum);
        
        boolean result = switch (exerciseNum) {
            // Блок 1: Events
            case 1 -> isTrue("submitClicked");
            case 2 -> arrayContains("linkTexts", "О проекте");
            case 3 -> isTrue("hoveredMenu");
            case 4 -> validateCancelBtn();
            case 5 -> arrayContains("navClicks", "home");

            // Блок 2: DOM changes
            case 6  -> {
                String initialValue = (String) executeJavaScript(
                    "return window.__initialState?.values?.['username'] || '';"
                );
                String currentValue = $("#username").getValue();
                boolean changed = !initialValue.equals(currentValue);
                boolean hasCorrectValue = "Ivan".equals(currentValue);
                System.out.println("[Validator] #username: initial='" + initialValue + "', current='" + currentValue + "'");
                yield changed && hasCorrectValue;
            }
            case 7  -> domCheck("#notes",        "TEXT_CONTAINS", "Купить хлеб");
            case 8  -> {
                String initialValue = (String) executeJavaScript(
                    "return window.__initialState?.values?.['search'] || '';"
                );
                String currentValue = $("#search").getValue();
                boolean wasNonEmpty = initialValue != null && !initialValue.isEmpty();
                boolean nowEmpty = currentValue == null || currentValue.isEmpty();
                System.out.println("[Validator] #search: initial='" + initialValue + "', current='" + currentValue + "'");
                yield wasNonEmpty && nowEmpty;
            }
            case 9  -> domCheck("#search-input", "VALUE",         "Selenide");
            case 10 -> {
                String currentValue = $("#user-email").getValue();
                boolean hasCorrectValue = "test@example.com".equals(currentValue);
                System.out.println("[Validator] #user-email: current='" + currentValue + "'");
                yield hasCorrectValue;
            }
            case 11 -> domCheck("#notes",        "PLACEHOLDER",   "Ваши заметки");

            // Блок 3: Assertions — только статическая проверка
            case 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 -> true;

            // Блок 4.1: Events через JS-трекеры
            case 23 -> arrayContains("linkTexts", "Prices");
            case 27 -> isTrue("firstBtnClicked");

            // Блок 4.1: Коллекции — только статическая проверка
            case 22, 24, 25, 26, 28, 29, 30, 31, 32 -> true;

            // Блок 4.2: Dropdown — студент меняет выбор, проверяем DOM
            case 33 -> validateDropdownSelectedText("#dropdown-city", "Санкт-Петербург");
            case 34 -> validateDropdownSelectedValue("#dropdown-country", "ru");
            case 35 -> {
                int currentIndex = ((Long) executeJavaScript(
                    "return document.querySelector('#dropdown').selectedIndex;"
                )).intValue();
                int initialIndex = ((Long) executeJavaScript(
                    "const el = document.querySelector('#dropdown');" +
                    "const initVal = window.__initialState?.values?.['dropdown'] || '';" +
                    "for(let i = 0; i < el.options.length; i++) { if(el.options[i].value === initVal) return i; }" +
                    "return -1;"
                )).intValue();
                boolean changed = currentIndex != initialIndex;
                boolean isLast = currentIndex == 2;
                System.out.println("[Validator] #dropdown: initialIndex=" + initialIndex + ", currentIndex=" + currentIndex);
                yield isLast && changed;
            }
            case 37 -> validateDropdownSelectedTexts("#dropdown-multi", List.of("Red", "Green"));
            case 38 -> validateDropdownSelectedTextContains("#dropdown-country", "Germany");

            // Блок 4.2: Dropdown — только статическая проверка
            case 36, 39 -> true;

            // Блок 5: Чекбоксы и радиокнопки
            case 40 -> validateCheckboxChecked("#agree-checkbox");
            case 41 -> validateCheckboxNotChecked("#newsletter-checkbox");
            case 42, 43 -> true;
            case 44 -> {
                boolean selectedNow = $("#payment-card").isSelected();
                boolean selectedBefore = Boolean.TRUE.equals(executeJavaScript(
                    "return window.__initialState?.checked?.['payment-card'] || false;"
                ));
                System.out.println("[Validator] #payment-card: before=" + selectedBefore + ", now=" + selectedNow);
                yield selectedNow && !selectedBefore;
            }
            case 45 -> {
                boolean selectedNow = $("input[name='payment'][value='cash']").isSelected();
                Boolean selectedBefore = (Boolean) executeJavaScript(
                    "const el = document.querySelector(\"input[name='payment'][value='cash']\"); " +
                    "if (!el) return false; " +
                    "const key = el.id || el.className; " +
                    "return window.__initialState?.checked?.[key] || false;"
                );
                System.out.println("[Validator] payment[cash]: before=" + selectedBefore + ", now=" + selectedNow);
                yield selectedNow && !Boolean.TRUE.equals(selectedBefore);
            }
            case 46 -> validateCheckboxChecked("#hidden-checkbox");
            case 47 -> true;
            case 48 -> {
                boolean checkedNow = $("#toggle-checkbox").isSelected();
                boolean checkedBefore = Boolean.TRUE.equals(executeJavaScript(
                    "return window.__initialState?.checked?.['toggle-checkbox'] || false;"
                ));
                System.out.println("[Validator] #toggle-checkbox: before=" + checkedBefore + ", now=" + checkedNow);
                yield checkedNow && !checkedBefore;
            }
            case 49 -> true;
            case 50 -> {
                boolean selectedNow = $("#option-b").isSelected();
                boolean selectedBefore = Boolean.TRUE.equals(executeJavaScript(
                    "return window.__initialState?.checked?.['option-b'] || false;"
                ));
                System.out.println("[Validator] #option-b: before=" + selectedBefore + ", now=" + selectedNow);
                yield selectedNow && !selectedBefore;
            }
            case 51 -> true;

            // Блок 6: Сложные действия — фронтенд выставляет маркеры в DOM
            case 52 -> validateAttributeValue("#menu-item", "data-hovered", "true");
            case 53 -> validateAttributeValue("#editable-text", "data-edited", "true");
            case 54 -> validateAttributeValue("#area", "data-context-clicked", "true");
            case 55 -> validateAttributeValue("#source", "data-dropped", "true")
                    && validateAttributeValue("#target", "data-dropped-on", "true");
            case 56 -> validateClassAndAttribute(".file", "selected", "data-selected", "true");
            case 57 -> validateAttributeValue("#tooltip", "data-tooltip-visible", "true");
            case 58 -> validateSliderMoved("#slider-handle");
            case 59 -> validateResized("#resizable");

            // Блок 7: Браузерные alert/confirm/prompt
            case 60 -> isTrue("alertBtnClicked") && validateDomText("#alert-result", "Alert принят");
            case 61 -> isTrue("alertTextBtnClicked") && validateDomText("#alert-text-result", "Текст прочитан");
            case 62 -> isTrue("deleteBtnClicked") && isTrue("deleteCancelled");
            case 63 -> isTrue("promptBtnClicked") && isTrue("promptFilled");
            case 64 -> isTrue("alertAccepted") && isTrue("confirmAccepted") && isTrue("promptFilled")
                    && validateDomContains("#chain-result", "Готово! Имя:");
            case 65 -> isTrue("promptDismissBtnClicked") && isTrue("promptDismissed");
            case 66 -> isTrue("confirmBtnClicked") && isTrue("confirmAccepted");
            case 67 -> isTrue("alertDismissBtnClicked") && isTrue("alertDismissed");
            case 68 -> {
                boolean dismissed = Boolean.TRUE.equals(executeJavaScript("return window.__uiState?.validationDismissed;"));
                String email = $("#email-input").getValue();
                boolean valuePreserved = "invalid-email".equals(email);
                System.out.println("[Validator] Exercise 68: dismissed=" + dismissed + ", email='" + email + "'");
                yield dismissed && valuePreserved;
            }
            case 69 -> isTrue("modalOpened") && validateDomText("#modal-opened-result", "Модальное окно открыто");
            case 70 -> isTrue("modalOpened") && isTrue("modalClosedByEsc");
            case 71 -> isTrue("modalOpened") && isTrue("modalClosedByBtn");
            case 72 -> isTrue("modalOpened") && isTrue("modalClosedByOverlay");
            case 73 -> isTrue("formModalOpened") && isTrue("formSubmitted");
            case 74 -> isTrue("firstModalOpened") && isTrue("secondModalOpened") && isTrue("cascadeModalClosed");

            // Блок 8: IFrame
            case 75 -> {
                switchTo().defaultContent();
                switchTo().frame($("#payment-frame"));
                boolean clicked = Boolean.TRUE.equals(executeJavaScript("return window.__uiState?.payBtnClicked"));
                String dataClicked = $("#pay-btn").getAttribute("data-clicked");
                switchTo().defaultContent();
                yield clicked && "true".equals(dataClicked);
            }
            case 76 -> {
                switchTo().defaultContent();
                yield validateElementClicked("#exit-btn");
            }
            case 77 -> {
                switchTo().defaultContent();
                $("#outer-frame").should(exist, Duration.ofSeconds(5));
                switchTo().frame($("#outer-frame"));
                $("#inner-frame").should(exist);
                switchTo().frame($("#inner-frame"));
                boolean filled = Boolean.TRUE.equals(executeJavaScript("return window.__uiState?.inputFilled"));
                String value = $("#nested-input").getValue();
                switchTo().defaultContent();
                yield filled && value != null && !value.isEmpty();
            }
            case 78 -> {
                switchTo().defaultContent();
                switchTo().frame($("#main-frame"));
                String text = $("h1").getText();
                switchTo().defaultContent();
                yield "Welcome".equals(text);
            }
            case 79 -> {
                switchTo().defaultContent();
                switchTo().frame($("#outer-frame"));
                switchTo().frame($("#inner-frame"));
                boolean changed = Boolean.TRUE.equals(executeJavaScript("return window.__uiState?.inputFilled"));
                String value = $("#input-inside").getValue();
                switchTo().parentFrame();
                String parentTitle = $("#parent-title").getText();
                switchTo().defaultContent();
                boolean parentOk = parentTitle != null && parentTitle.contains("Родительский");
                System.out.println("[Validator] Exercise 79: changed=" + changed + ", valueChanged=" + !value.equals("Исходное значение") + ", parentTitle='" + parentTitle + "', parentOk=" + parentOk);
                yield changed && value != null && !value.equals("Исходное значение") && parentOk;
            }
            case 80 -> {
                switchTo().defaultContent();
                $("#dynamic-frame").should(exist, Duration.ofSeconds(5));
                switchTo().frame($("#dynamic-frame"));
                boolean loaded = Boolean.TRUE.equals(executeJavaScript("return window.__uiState?.frameLoaded"));
                switchTo().defaultContent();
                boolean visible = $("#dynamic-frame").isDisplayed();
                yield loaded && visible;
            }

            // Блок 9: Тестовые вопросы — только статическая проверка
            case 81, 82, 83, 84, 85, 86, 87, 88, 89, 90 -> true;

            default -> {
                System.out.println("[TestRunner] Неизвестное упражнение: " + exerciseNum);
                yield false;
            }
        };
        
        if (!result) {
            try {
                String screenshotPath = "validation_failed_exercise_" + exerciseNum + "_" + System.currentTimeMillis() + ".png";
                Selenide.screenshot(screenshotPath);
                System.err.println("[TestRunner] Скриншот неудачной валидации сохранен: " + screenshotPath);
            } catch (Exception screenshotError) {
                System.err.println("[TestRunner] Не удалось сделать скриншот неудачной валидации: " + screenshotError.getMessage());
            }
        }
        
        return result;
    }

    /** @param key ключ в {@code window.__uiState}; @return true, если флаг установлен */
    private static boolean isTrue(String key) {
        Object result = executeJavaScript("return window.__uiState?.['" + key + "'] || false;");
        System.out.println("[Validator] " + key + ": " + result);
        return Boolean.TRUE.equals(result);
    }

    /** Проверяет наличие значения в массиве {@code window.__uiState[key]} */
    private static boolean arrayContains(String key, String value) {
        Long count = (Long) executeJavaScript(
            "return (window.__uiState?.['" + key + "'] || []).filter(x => x?.includes('" + value + "')).length;"
        );
        boolean result = count != null && count > 0;
        System.out.println("[Validator] " + key + " contains '" + value + "': " + result);
        return result;
    }

    /** Проверяет клик по кнопке Cancel с учётом видимости и доступности */
    private static boolean validateCancelBtn() {
        boolean clicked = isTrue("cancelClicked");
        Object visibleObj = executeJavaScript("return window.__uiState?.cancelVisibleAtClick || false;");
        Object enabledObj = executeJavaScript("return window.__uiState?.cancelEnabledAtClick || false;");
        boolean visible = Boolean.TRUE.equals(visibleObj);
        boolean enabled = Boolean.TRUE.equals(enabledObj);
        boolean result = clicked && visible && enabled;
        System.out.println("[Validator] cancelBtn: clicked=" + clicked + ", visible=" + visible + ", enabled=" + enabled + " => " + result);
        return result;
    }

    /**
     * Универсальная DOM-проверка по типу (value, text, visible, attribute и т.д.)
     * @param type тип проверки
     * @param expected ожидаемое значение
     */
    private static boolean domCheck(String selector, String type, String expected) {
        try {
            SelenideElement el = $(selector);

            if ("NOT_EXIST".equals(type)) {
                boolean exists = el.exists();
                System.out.println("[Validator] NOT_EXIST " + selector + ": exists=" + exists);
                return !exists;
            }

            if (!el.exists()) {
                System.out.println("[Validator] FAIL: element not found: " + selector);
                return false;
            }

            return switch (type) {
                case "VALUE" -> {
                    String value = el.getValue();
                    boolean match = expected.equals(value);
                    System.out.println("[Validator] VALUE " + selector + ": actual='" + value + "', expected='" + expected + "'");
                    yield match;
                }
                case "TEXT_CONTAINS" -> {
                    String text = el.getValue();
                    if (text == null || text.isEmpty()) text = el.getText();
                    boolean contains = text != null && text.contains(expected);
                    System.out.println("[Validator] TEXT_CONTAINS " + selector + ": '" + text + "' contains '" + expected + "': " + contains);
                    yield contains;
                }
                case "TEXT" -> {
                    String text = el.getText();
                    if (text == null || text.isEmpty()) text = el.getValue();
                    boolean contains = text != null && text.contains(expected);
                    System.out.println("[Validator] TEXT " + selector + ": actual='" + text + "', expected='" + expected + "'");
                    yield contains;
                }
                case "PLACEHOLDER" -> {
                    String placeholder = el.getAttribute("placeholder");
                    boolean match = expected.equals(placeholder);
                    System.out.println("[Validator] PLACEHOLDER " + selector + ": actual='" + placeholder + "'");
                    yield match;
                }
                case "VISIBLE" -> {
                    boolean visible = el.isDisplayed();
                    System.out.println("[Validator] VISIBLE " + selector + ": " + visible);
                    yield visible;
                }
                case "DISABLED" -> {
                    boolean disabled = !el.isEnabled();
                    System.out.println("[Validator] DISABLED " + selector + ": " + disabled);
                    yield disabled;
                }
                case "ATTRIBUTE" -> {
                    String[] parts = expected.split("=", 2);
                    if (parts.length != 2) yield false;
                    String attrValue = el.getAttribute(parts[0]);
                    boolean match = parts[1].equals(attrValue);
                    System.out.println("[Validator] ATTRIBUTE " + selector + " [" + parts[0] + "]: actual='" + attrValue + "'");
                    yield match;
                }
                case "CLASS" -> {
                    String cls = el.getAttribute("class");
                    String key = selector.replaceFirst("^[#.]", "");
                    String initCls = (String) executeJavaScript(
                        "return window.__initialState?.classes['" + key + "'] || '';"
                    );
                    boolean hasNow = cls != null && cls.contains(expected);
                    boolean hadBefore = initCls != null && initCls.contains(expected);
                    System.out.println("[Validator] CLASS " + selector + ": now='" + cls + "', initial='" + initCls + "'");
                    yield hasNow && !hadBefore;
                }
                case "DISAPPEAR" -> {
                    long start = System.currentTimeMillis();
                    boolean disappeared = false;
                    while (System.currentTimeMillis() - start < 5000) {
                        try {
                            if (!el.exists() || !el.isDisplayed()) {
                                disappeared = true;
                                break;
                            }
                            Thread.sleep(200);
                        } catch (Exception ignored) {
                            disappeared = true;
                            break;
                        }
                    }
                    System.out.println("[Validator] DISAPPEAR " + selector + ": " + disappeared);
                    yield disappeared;
                }
                default -> {
                    System.out.println("[Validator] Unknown type: " + type);
                    yield false;
                }
            };
        } catch (Exception e) {
            System.err.println("[Validator] domCheck " + selector + " ERROR: " + e.getMessage());
            return "NOT_EXIST".equals(type);
        }
    }

    /** Проверяет текст выбранной опции выпадающего списка */
    private static boolean validateDropdownSelectedText(String selector, String expectedText) {
        try {
            String actualText = $(selector).getSelectedOption().getText().trim();
            boolean result = actualText.equals(expectedText);
            System.out.println("[Validator] Dropdown selected text: expected='" + expectedText + "', actual='" + actualText + "'");
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDropdownSelectedText ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет value выбранной опции */
    private static boolean validateDropdownSelectedValue(String selector, String expectedValue) {
        try {
            String actualValue = $(selector).getSelectedOption().getAttribute("value");
            boolean result = expectedValue.equals(actualValue);
            System.out.println("[Validator] Dropdown selected value: expected='" + expectedValue + "', actual='" + actualValue + "'");
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDropdownSelectedValue ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет индекс выбранной опции */
    private static boolean validateDropdownSelectedIndex(String selector, int expectedIndex) {
        try {
            Object result = executeJavaScript(
                "return document.querySelector('" + selector + "').selectedIndex;"
            );
            int actualIndex = result instanceof Long ? ((Long) result).intValue() : -1;
            boolean match = actualIndex == expectedIndex;
            System.out.println("[Validator] Dropdown selected index: expected=" + expectedIndex + ", actual=" + actualIndex);
            return match;
        } catch (Exception e) {
            System.err.println("[Validator] validateDropdownSelectedIndex ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет тексты всех выбранных опций (multiselect) */
    private static boolean validateDropdownSelectedTexts(String selector, List<String> expectedTexts) {
        try {
            ElementsCollection selectedOptions = $(selector).getSelectedOptions();
            List<String> actualTexts = new ArrayList<>();
            for (SelenideElement el : selectedOptions) {
                actualTexts.add(el.getText().trim());
            }
            boolean result = actualTexts.containsAll(expectedTexts);
            System.out.println("[Validator] Dropdown selected texts: expected=" + expectedTexts + ", actual=" + actualTexts);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDropdownSelectedTexts ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что текст выбранной опции содержит подстроку */
    private static boolean validateDropdownSelectedTextContains(String selector, String expectedText) {
        try {
            String actualText = $(selector).getSelectedOption().getText().trim();
            boolean result = actualText.contains(expectedText);
            System.out.println("[Validator] Dropdown selected text contains '" + expectedText + "': actual='" + actualText + "'");
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDropdownSelectedTextContains ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что чекбокс отмечен */
    private static boolean validateCheckboxChecked(String selector) {
        try {
            boolean isChecked = $(selector).isSelected();
            System.out.println("[Validator] Checkbox checked " + selector + ": " + isChecked);
            return isChecked;
        } catch (Exception e) {
            System.err.println("[Validator] validateCheckboxChecked ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что чекбокс не отмечен */
    private static boolean validateCheckboxNotChecked(String selector) {
        try {
            boolean isChecked = $(selector).isSelected();
            System.out.println("[Validator] Checkbox not checked " + selector + ": " + !isChecked);
            return !isChecked;
        } catch (Exception e) {
            System.err.println("[Validator] validateCheckboxNotChecked ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что радиокнопка выбрана */
    private static boolean validateRadioSelected(String selector) {
        try {
            boolean isSelected = $(selector).isSelected();
            System.out.println("[Validator] Radio selected " + selector + ": " + isSelected);
            return isSelected;
        } catch (Exception e) {
            System.err.println("[Validator] validateRadioSelected ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что все чекбоксы по селектору отмечены */
    private static boolean validateAllCheckboxesChecked(String selector) {
        try {
            ElementsCollection elements = $$(selector);
            for (SelenideElement el : elements) {
                if (!el.isSelected()) {
                    System.out.println("[Validator] Not all checkboxes checked: " + selector);
                    return false;
                }
            }
            System.out.println("[Validator] All " + elements.size() + " checkboxes checked: " + selector);
            return true;
        } catch (Exception e) {
            System.err.println("[Validator] validateAllCheckboxesChecked ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что отмечен хотя бы один чекбокс из группы */
    private static boolean validateAtLeastOneChecked(String selector) {
        try {
            ElementsCollection elements = $$(selector);
            for (SelenideElement el : elements) {
                if (el.isSelected()) {
                    System.out.println("[Validator] At least one checked in " + selector);
                    return true;
                }
            }
            System.out.println("[Validator] No checkboxes checked in " + selector);
            return false;
        } catch (Exception e) {
            System.err.println("[Validator] validateAtLeastOneChecked ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет значение HTML-атрибута элемента */
    private static boolean validateAttributeValue(String selector, String attribute, String expectedValue) {
        try {
            String actualValue = $(selector).getAttribute(attribute);
            boolean result = expectedValue.equals(actualValue);
            System.out.println("[Validator] Attribute [" + attribute + "] on " + selector + ": expected='" + expectedValue + "', actual='" + actualValue + "'");
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateAttributeValue ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет наличие CSS-класса и значения атрибута одновременно */
    private static boolean validateClassAndAttribute(String selector, String expectedClass,
                                                      String attribute, String expectedValue) {
        try {
            SelenideElement el = $(selector);
            String cls = el.getAttribute("class");
            String attrValue = el.getAttribute(attribute);
            boolean hasClass = cls != null && cls.contains(expectedClass);
            boolean hasAttr = expectedValue.equals(attrValue);
            boolean result = hasClass && hasAttr;
            System.out.println("[Validator] Class+Attribute on " + selector
                + ": class='" + cls + "', " + attribute + "='" + attrValue + "' => " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateClassAndAttribute ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что ползунок сдвинут (атрибут data-position) */
    private static boolean validateSliderMoved(String selector) {
        try {
            String positionStr = $(selector).getAttribute("data-position");
            if (positionStr == null) {
                System.out.println("[Validator] Slider not moved: data-position is null");
                return false;
            }
            int position = Integer.parseInt(positionStr);
            int expected = 100;
            boolean result = Math.abs(position - expected) <= 10;
            System.out.println("[Validator] Slider moved: data-position=" + position + " (expected ~" + expected + ", ±10)");
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateSliderMoved ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет изменение размера элемента (data-height-before/after) */
    private static boolean validateResized(String selector) {
        try {
            SelenideElement el = $(selector);
            String heightBefore = el.getAttribute("data-height-before");
            String heightAfter = el.getAttribute("data-height-after");
            if (heightBefore == null || heightAfter == null) {
                System.out.println("[Validator] Resize not detected: before='" + heightBefore + "', after='" + heightAfter + "'");
                return false;
            }
            int before = Integer.parseInt(heightBefore);
            int after = Integer.parseInt(heightAfter);
            int diff = Math.abs(after - before);
            int expected = 30;
            boolean result = Math.abs(diff - expected) <= 5;
            System.out.println("[Validator] Resized: before=" + before + ", after=" + after + ", diff=" + diff + " (expected ~" + expected + ", ±5) => " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateResized ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет точное совпадение текста элемента */
    private static boolean validateDomText(String selector, String expectedText) {
        try {
            var el = $(selector);
            String actualText = el.getText().trim();
            boolean result = expectedText.equals(actualText);
            System.out.println("[Validator] DOM text " + selector + ": expected='" + expectedText + "', actual='" + actualText + "' => " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDomText ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Проверяет, что текст элемента содержит подстроку */
    private static boolean validateDomContains(String selector, String expectedSubstring) {
        try {
            var el = $(selector);
            String actualText = el.getText().trim();
            boolean result = actualText.contains(expectedSubstring);
            System.out.println("[Validator] DOM contains " + selector + ": expected contains '" + expectedSubstring + "', actual='" + actualText + "' => " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateDomContains ERROR: " + e.getMessage());
            return false;
        }
    }

    /** Определяет baseUrl: WSL2, Docker, переменные окружения или localhost */
    private static String resolveBaseUrl() {
        if (isWsl2()) {
            String wslIp = getWsl2HostIp();
            if (wslIp != null) {
                return "http://" + wslIp + ":5173";
            }
            return "http://10.255.255.254:5173";
        }
        if (System.getenv("RUNNER_DOCKER") != null || System.getenv("FRONTEND_URL") != null) {
            String env = System.getenv("FRONTEND_URL");
            if (env != null && !env.isEmpty()) {
                return env;
            }
            return "http://frontend:5173";
        }

        String prop = System.getProperty("selenide.baseUrl");
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }

        String env = System.getenv("SELENIDE_BASE_URL");
        if (env != null && !env.isEmpty()) {
            return env;
        }

        return "http://localhost:5173";
    }

    /** @return true, если раннер запущен в WSL2 */
    private static boolean isWsl2() {
        try {
            java.nio.file.Path osReleasePath = java.nio.file.Path.of("/proc/sys/kernel/osrelease");
            if (!java.nio.file.Files.exists(osReleasePath)) {
                return false;
            }
            
            String osRelease = new String(java.nio.file.Files.readAllBytes(osReleasePath));
            return osRelease.toLowerCase().contains("microsoft");
        } catch (Exception e) {
            return false;
        }
    }

    /** @return IP хоста Windows для доступа к frontend из WSL2 */
    private static String getWsl2HostIp() {
        try {
            java.nio.file.Path resolvPath = java.nio.file.Path.of("/etc/resolv.conf");
            if (!java.nio.file.Files.exists(resolvPath)) {
                return null;
            }
            
            String resolv = new String(java.nio.file.Files.readAllBytes(resolvPath));
            
            for (String line : resolv.split("\n")) {
                line = line.trim();
                if (line.contains("ExtServers:") && line.contains("host(")) {
                    int start = line.indexOf("host(") + 5;
                    int end = line.indexOf(")", start);
                    if (start > 4 && end > start) {
                        String ip = line.substring(start, end).trim();
                        return ip;
                    }
                }
            }
        
            for (String line : resolv.split("\n")) {
                line = line.trim();
                if (line.startsWith("nameserver")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 2) {
                        String ip = parts[1].trim();
                        if (!ip.startsWith("127.") && !ip.startsWith("::1")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /** Проверяет клик по элементу (атрибут data-clicked=true) */
    private static boolean validateElementClicked(String selector) {
        try {
            String dataClicked = $(selector).getAttribute("data-clicked");
            boolean result = "true".equals(dataClicked);
            System.out.println("[Validator] Element clicked " + selector + ": " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[Validator] validateElementClicked ERROR: " + e.getMessage());
            return false;
        }
    }
}

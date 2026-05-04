package com.example.runner.harness.module3;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.url;

public abstract class AbstractHarnessTest {

    protected static final Logger log = LoggerFactory.getLogger(AbstractHarnessTest.class);

    private static final String DOCKER_FRONTEND = "http://frontend:5173";
    private static final String WSL2_HOST = "http://172.17.0.1:5173";
    private static final String LOCALHOST = "http://localhost:5173";

    protected static String BASE_URL;

    protected static ClassLoader studentClassLoader;

    public static void setStudentClassLoader(ClassLoader cl) {
        studentClassLoader = cl;
    }

    public static ClassLoader getStudentClassLoader() {
        return studentClassLoader;
    }

    protected static String resolveBaseUrl() {
        if (System.getenv("RUNNER_DOCKER") != null || System.getenv("FRONTEND_URL") != null) {
            String env = System.getenv("FRONTEND_URL");
            if (env != null && !env.isEmpty()) {
                return env;
            }
            return DOCKER_FRONTEND;
        }

        String prop = System.getProperty("selenide.baseUrl");
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }

        String env = System.getenv("SELENIDE_BASE_URL");
        if (env != null && !env.isEmpty()) {
            return env;
        }

        if (isWsl2()) {
            String wslIp = getWsl2HostIp();
            if (wslIp != null) {
                return "http://" + wslIp + ":5173";
            }
            return WSL2_HOST;
        }

        return LOCALHOST;
    }

    protected static boolean isWsl2() {
        try {
            String osRelease = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Path.of("/proc/sys/kernel/osrelease")));
            return osRelease.toLowerCase().contains("microsoft");
        } catch (Exception e) {
            return false;
        }
    }

    protected static String getWsl2HostIp() {
        try {
            String resolv = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Path.of("/etc/resolv.conf")));
            for (String line : resolv.split("\n")) {
                if (line.startsWith("nameserver")) {
                    return line.split("\\s+")[1].trim();
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUpSelenide() throws Exception {
        BASE_URL = resolveBaseUrl();

        Configuration.baseUrl = BASE_URL;
        Configuration.browser = "chrome";
        Configuration.headless = true;
        Configuration.timeout = 8000;
        Configuration.pageLoadStrategy = "eager";
        Configuration.browserCapabilities.setCapability("pageLoadStrategy", "eager");
        Configuration.browserCapabilities.setCapability("goog:chromeOptions",
                new org.openqa.selenium.chrome.ChromeOptions()
                        .addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"));

        String pageUrl = getPageUrl();
        if (pageUrl != null && !pageUrl.isEmpty()) {
            Selenide.open(BASE_URL + pageUrl);
        } else {
            Selenide.open(BASE_URL);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected static void openPage(String path) {
        Selenide.open(BASE_URL + path);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Class<?> loadStudentClass(String className) throws Exception {
        if (studentClassLoader == null) {
            throw new IllegalStateException("studentClassLoader не установлен. Вызовите setStudentClassLoader() перед загрузкой.");
        }
        return studentClassLoader.loadClass(className);
    }

    protected abstract String getPageUrl();

    protected static Object invokeStatic(Class<?> cls, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        java.lang.reflect.Method method = cls.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    protected static Object invoke(Object target, String methodName, Object... args) throws Exception {
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        java.lang.reflect.Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    protected void assertVisible(String selector) {
        $(selector).shouldBe(visible);
    }

    protected void assertValue(String selector, String expected) {
        $(selector).shouldHave(value(expected));
    }

    protected void assertText(String selector, String expected) {
        $(selector).shouldHave(com.codeborne.selenide.Condition.text(expected));
    }
}

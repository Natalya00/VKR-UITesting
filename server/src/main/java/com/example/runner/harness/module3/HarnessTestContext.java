package com.example.runner.harness.module3;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class HarnessTestContext {

    private static final ThreadLocal<ClassLoader> LOADER = new ThreadLocal<>();

    public static void set(Path classesDir) throws Exception {
        URL[] urls = {classesDir.toUri().toURL()};
        ClassLoader parent = HarnessTestContext.class.getClassLoader();
        URLClassLoader loader = new URLClassLoader(urls, parent);
        LOADER.set(loader);
        AbstractHarnessTest.studentClassLoader = loader;
    }

    public static ClassLoader get() {
        ClassLoader loader = LOADER.get();
        return loader != null ? loader : HarnessTestContext.class.getClassLoader();
    }

    public static void clear() {
        LOADER.remove();
    }
}

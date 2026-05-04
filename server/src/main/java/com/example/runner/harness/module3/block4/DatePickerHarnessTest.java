package com.example.runner.harness.module3.block4;

import com.example.runner.harness.module3.AbstractHarnessTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static com.codeborne.selenide.Selenide.$;
import static org.junit.jupiter.api.Assertions.*;

public class DatePickerHarnessTest extends AbstractHarnessTest {

    @Override
    protected String getPageUrl() {
        return "/test-harness/module3/components";
    }

    private Object createDatePicker() throws Exception {
        Class<?> cls = loadStudentClass("com.example.runner.pom.components.DatePicker");
        Constructor<?> ctor = cls.getDeclaredConstructor(
                com.codeborne.selenide.SelenideElement.class);
        return ctor.newInstance($("#date-picker"));
    }

    @Test
    void testSelectDay() throws Exception {
        Object dp = createDatePicker();

        invoke(dp, "selectDay", "15");

        String selected = $(".datepicker-day").getValue();
        assertEquals("15", selected,
                "selectDay('15') должен выбрать option со значением 15");
    }

    @Test
    void testSelectMonth() throws Exception {
        Object dp = createDatePicker();

        invoke(dp, "selectMonth", "Март");

        String selected = $(".datepicker-month").getValue();
        assertEquals("Март", selected,
                "selectMonth('Март') должен выбрать option со значением Март");
    }

    @Test
    void testSelectYear() throws Exception {
        Object dp = createDatePicker();

        invoke(dp, "selectYear", "2024");

        String selected = $(".datepicker-year").getValue();
        assertEquals("2024", selected,
                "selectYear('2024') должен выбрать option со значением 2024");
    }

    @Test
    void testSelectDate() throws Exception {
        Object dp = createDatePicker();

        invoke(dp, "selectDate", "25", "Декабрь", "2025");

        String day = $(".datepicker-day").getValue();
        String month = $(".datepicker-month").getValue();
        String year = $(".datepicker-year").getValue();

        assertEquals("25", day, "selectDate() должен выбрать день 25");
        assertEquals("Декабрь", month, "selectDate() должен выбрать месяц Декабрь");
        assertEquals("2025", year, "selectDate() должен выбрать год 2025");
    }

    @Test
    void testSelectDateReturnsThis() throws Exception {
        Object dp = createDatePicker();
        Object result = invoke(dp, "selectDate", "10", "Январь", "2025");
        assertSame(dp, result, "selectDate() должен возвращать this для цепочки вызовов");
    }
}

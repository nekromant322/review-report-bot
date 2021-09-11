package com.nekromant.telegram.utils;

import java.time.format.DateTimeFormatter;

public class FormatterUtils {

    public static DateTimeFormatter defaultDateFormatter() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy");
    }

    public static DateTimeFormatter defaultDateTimeFormatter() {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    }
}

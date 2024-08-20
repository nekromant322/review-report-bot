package com.nekromant.telegram.utils;

import java.security.InvalidParameterException;

public class ValidationUtils {

    public static void validateArguments(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException("Wrong arguments count");
        }
        int i = Integer.parseInt(strings[1]);
        if (i < 0 || i > 24) {
            throw new InvalidParameterException("Неверное значение часов — должно быть от 0 до 24");
        }
    }
}

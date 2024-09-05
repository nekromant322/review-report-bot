package com.nekromant.telegram.utils;

import java.security.InvalidParameterException;

public class ValidationUtils {

    public static void validateArgumentsNumber(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException("Wrong arguments count");
        }
    }
}

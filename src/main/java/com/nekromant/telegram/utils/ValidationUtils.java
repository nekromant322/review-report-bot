package com.nekromant.telegram.utils;

import java.security.InvalidParameterException;

import static com.nekromant.telegram.contants.MessageContants.WRONG_ARGUMENTS_COUNT;

public class ValidationUtils {

    public static void validateArgumentsNumber(String[] strings) {
        if (strings == null || strings.length == 0) {
            throw new InvalidParameterException(WRONG_ARGUMENTS_COUNT);
        }
    }
}

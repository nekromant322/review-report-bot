package com.nekromant.telegram.exception;

public class TooManyReportsException extends RuntimeException {
    public TooManyReportsException() {
        super("За этот день уже есть отчет");
    }
}

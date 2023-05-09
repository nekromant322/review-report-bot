package com.nekromant.telegram.utils;

import org.hibernate.dialect.PostgreSQL81Dialect;

import java.sql.Types;

public class CustomDialectUtils extends PostgreSQL81Dialect {
    public CustomDialectUtils() {
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");
    }
}

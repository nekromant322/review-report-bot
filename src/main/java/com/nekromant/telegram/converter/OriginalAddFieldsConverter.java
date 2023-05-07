package com.nekromant.telegram.converter;

import com.google.gson.Gson;
import com.nekromant.telegram.model.OriginalAddFields;

import javax.persistence.AttributeConverter;

public class OriginalAddFieldsConverter implements AttributeConverter<OriginalAddFields, String> {
    private final static Gson GSON = new Gson();

    @Override
    public String convertToDatabaseColumn(OriginalAddFields addFields) {
        return GSON.toJson(addFields);
    }

    @Override
    public OriginalAddFields convertToEntityAttribute(String s) {
        return GSON.fromJson(s, OriginalAddFields.class);
    }
}

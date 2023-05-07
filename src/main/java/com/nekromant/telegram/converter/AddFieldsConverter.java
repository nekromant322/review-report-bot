package com.nekromant.telegram.converter;

import com.google.gson.Gson;
import com.nekromant.telegram.model.AddFields;

import javax.persistence.AttributeConverter;

public class AddFieldsConverter implements AttributeConverter<AddFields, String> {
    private final static Gson GSON = new Gson();

    @Override
    public String convertToDatabaseColumn(AddFields addFields) {
        return GSON.toJson(addFields);
    }

    @Override
    public AddFields convertToEntityAttribute(String s) {
        return GSON.fromJson(s, AddFields.class);
    }
}

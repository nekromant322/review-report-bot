package com.nekromant.telegram.converter;

import com.google.gson.Gson;
import com.nekromant.telegram.model.Purchase;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PurchaseConverter implements AttributeConverter<Purchase, String> {
    private final static Gson GSON = new Gson();

    @Override
    public String convertToDatabaseColumn(Purchase purchase) {
        return GSON.toJson(purchase);
    }

    @Override
    public Purchase convertToEntityAttribute(String s) {
        return GSON.fromJson(s, Purchase.class);
    }
}

package com.nekromant.telegram.converter;

import com.google.gson.Gson;
import com.nekromant.telegram.model.Order;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class OrderConverter implements AttributeConverter<Order, String> {
    private final static Gson GSON = new Gson();

    @Override
    public String convertToDatabaseColumn(Order order) {
        return GSON.toJson(order);
    }

    @Override
    public Order convertToEntityAttribute(String s) {
        return GSON.fromJson(s, Order.class);
    }
}

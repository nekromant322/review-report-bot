package com.nekromant.telegram.converter;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class TimeSlotsConverter implements AttributeConverter<Set<Integer>, String> {
    private final String GROUP_DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<Integer> integerList) {
        if (integerList == null) {
            return "";
        }
        return integerList.stream().map(String::valueOf).collect(Collectors.joining(GROUP_DELIMITER));
    }

    @Override
    public Set<Integer> convertToEntityAttribute(String string) {
        return Arrays.stream(string.split(GROUP_DELIMITER)).map(Integer::parseInt).collect(Collectors.toSet());
    }
}

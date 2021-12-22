package com.rost.productwarehouse.itemproperty;

import java.util.Arrays;

public enum ItemPropertyValueDataType {

    STRING("string"), BOOLEAN("flag"), INTEGER("integer number"), BIG_DECIMAL("fractional number");

    private String name;

    ItemPropertyValueDataType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ItemPropertyValueDataType of(String str) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(str)).findFirst().orElse(STRING);
    }
}

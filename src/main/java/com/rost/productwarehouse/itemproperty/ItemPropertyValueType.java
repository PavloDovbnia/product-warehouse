package com.rost.productwarehouse.itemproperty;

import java.util.Arrays;

public enum ItemPropertyValueType {

    SINGLE("single"), MULTIPLE("multiple");

    private String name;

    ItemPropertyValueType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ItemPropertyValueType of(String str) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(str)).findFirst().orElse(MULTIPLE);
    }

}

package com.rost.productwarehouse.itemproperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ItemLevel {

    PRODUCT(0, "product"), PRODUCT_GROUP(1, "group"), MANUFACTURER(2, "manufacturer"), UNDEFINED(10, "undefined");

    private int priority;
    private String name;

    ItemLevel(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public static List<ItemLevel> levels() {
        return Arrays.stream(values()).filter(v -> !UNDEFINED.equals(v)).collect(Collectors.toList());
    }

    public static ItemLevel of(String str) {
        return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(str)).findFirst().orElse(UNDEFINED);
    }

    public static ItemLevel ofName(String str) {
        return Arrays.stream(values()).filter(v -> v.getName().equalsIgnoreCase(str)).findFirst().orElse(UNDEFINED);
    }
}

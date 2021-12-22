package com.rost.productwarehouse.itemproperty;

import java.util.List;

public class MultipleItemPropertyValue<T> implements ItemPropertyValue<T> {

    private List<T> values;

    public MultipleItemPropertyValue(List<T> values) {
        this.values = values;
    }

    @Override
    public List<T> getValue() {
        return values;
    }

    @Override
    public String toString() {
        return "MultipleItemPropertyValue{" +
                "values=" + values +
                '}';
    }
}

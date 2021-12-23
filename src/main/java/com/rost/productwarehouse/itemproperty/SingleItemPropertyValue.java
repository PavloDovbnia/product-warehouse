package com.rost.productwarehouse.itemproperty;

public class SingleItemPropertyValue<T> implements ItemPropertyValue<T> {

    private T value;

    public SingleItemPropertyValue(T value) {
        this.value = value;
    }

    public SingleItemPropertyValue() {
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SingleItemPropertyValue{" +
                "value=" + value +
                '}';
    }
}

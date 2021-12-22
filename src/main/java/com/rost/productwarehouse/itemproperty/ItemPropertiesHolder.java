package com.rost.productwarehouse.itemproperty;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

public class ItemPropertiesHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, ItemPropertyValue<?>> properties = Maps.newHashMap();

    public ItemPropertiesHolder() {
    }

    public ItemPropertiesHolder(Map<String, ItemPropertyValue<?>> properties) {
        this.properties = properties;
    }

    public <T> ItemPropertyValue<T> getPropertyValue(String propertyToken) {
        return (ItemPropertyValue<T>) properties.get(propertyToken);
    }

    public <T> void addProperties(Map<String, ItemPropertyValue<T>> properties) {
        this.properties.putAll(properties);
    }

    public <T> void addProperty(String propertyToken, ItemPropertyValue<T> value) {
        properties.put(propertyToken, value);
    }
}

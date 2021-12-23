package com.rost.productwarehouse.itemproperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ItemPropertyValueJsonDeserializer.class)
public interface ItemPropertyValue<T> {

    Object getValue();
}

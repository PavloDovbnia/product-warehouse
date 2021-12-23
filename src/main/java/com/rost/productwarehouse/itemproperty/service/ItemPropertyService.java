package com.rost.productwarehouse.itemproperty.service;

import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.ItemProperty;
import com.rost.productwarehouse.itemproperty.ItemPropertyValue;

import java.util.List;
import java.util.Map;

public interface ItemPropertyService {

    List<ItemProperty> getItemProperties(ItemLevel itemLevel);

    Map<Long, ItemPropertiesHolder> getPropertiesValues(List<Long> productsIds, ItemLevel itemLevel);

    void saveProperty(ItemProperty itemProperty);

    void deleteProperty(long id);

    void saveItemValues(long itemId, Map<String, ItemPropertyValue<?>> values, ItemLevel itemLevel);

    void deleteItemValues(long itemId, ItemLevel itemLevel);
}

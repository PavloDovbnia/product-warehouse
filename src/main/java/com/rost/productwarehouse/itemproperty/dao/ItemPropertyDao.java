package com.rost.productwarehouse.itemproperty.dao;

import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemProperty;

import java.util.List;

public interface ItemPropertyDao {

    ItemProperty getProperty(String token, ItemLevel itemLevel);

    List<ItemProperty> getProperties(List<ItemLevel> itemLevels);

    void saveProperty(ItemProperty itemProperty);

    void deleteProperty(long id);
}

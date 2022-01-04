package com.rost.productwarehouse.itemproperty.dao;

import com.rost.productwarehouse.itemproperty.ItemPropertyValue;

import java.util.List;
import java.util.Map;

public interface ItemPropertyValueDao {

    Map<Long, Map<String, ItemPropertyValue<?>>> getProductsProperties(List<Long> productsIds);

    Map<Long, Map<String, ItemPropertyValue<?>>> getGroupsProperties(List<Long> groupsIds);

    Map<Long, Map<String, ItemPropertyValue<?>>> getManufacturersProperties(List<Long> manufacturersIds);

    void saveProductValues(long productId, Map<String, String> values);

    void saveGroupValues(long groupId, Map<String, String> values);

    void saveManufacturerValues(long manufacturerId, Map<String, String> values);

    void deleteProductValues(long productId);

    void deleteGroupValues(long groupId);

    void deleteManufacturerValues(long manufacturerId);
}

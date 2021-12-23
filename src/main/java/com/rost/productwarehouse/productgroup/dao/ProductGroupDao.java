package com.rost.productwarehouse.productgroup.dao;

import com.rost.productwarehouse.productgroup.ProductGroup;

import java.util.Collection;
import java.util.List;

public interface ProductGroupDao {

    List<ProductGroup> getGroups();

    List<ProductGroup> getGroups(List<Long> groupsIds);

    long storeProductGroup(ProductGroup productGroup);

    void deleteProductGroup(long productGroupId);

    void storeGroupProducts(long groupId, Collection<Long> productsIds);

    void deleteProductsFromGroup(long groupId);

    void storeGroupCategory(long groupId, long categoryId);

    void deleteGroupCategory(long groupId);

    void deleteGroupFromManufacturer(long groupId);
}

package com.rost.productwarehouse.productgroup.dao;

import com.rost.productwarehouse.productgroup.ProductGroup;

import java.util.List;

public interface ProductGroupDao {

    List<ProductGroup> getGroups(List<Long> groupsIds);

    long storeProductGroup(ProductGroup productGroup);

    void deleteProductGroup(long productGroupId);
}

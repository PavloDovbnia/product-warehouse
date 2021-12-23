package com.rost.productwarehouse.productgroup.service;

import com.rost.productwarehouse.productgroup.ProductGroup;

import java.util.List;

public interface ProductGroupService {

    List<ProductGroup> getGroups();

    List<ProductGroup> getDecoratedGroups(List<Long> groupsIds);

    ProductGroup getDecoratedGroup(long groupId);

    long storeGroup(ProductGroup group);

    void deleteGroup(long groupId);
}

package com.rost.productwarehouse.productcategory.dao;

import com.rost.productwarehouse.productcategory.ProductCategory;

import java.util.List;

public interface ProductCategoryDao {

    List<ProductCategory> getCategories();

    long saveCategory(ProductCategory productCategory);

    void removeCategory(long categoryId);

    void removeCategoryGroups(long categoryId);
}

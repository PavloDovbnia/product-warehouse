package com.rost.productwarehouse.productcategory.service;

import com.rost.productwarehouse.productcategory.ProductCategory;

import java.util.List;

public interface ProductCategoryService {

    List<ProductCategory> getCategories();

    long saveCategory(ProductCategory productCategory);

    void removeCategory(long categoryId);
}

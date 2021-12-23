package com.rost.productwarehouse.productcategory.service;

import com.rost.productwarehouse.productcategory.ProductCategory;
import com.rost.productwarehouse.productcategory.dao.ProductCategoryDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryDao productCategoryDao;

    public ProductCategoryServiceImpl(ProductCategoryDao productCategoryDao) {
        this.productCategoryDao = productCategoryDao;
    }

    @Override
    public List<ProductCategory> getCategories() {
        return productCategoryDao.getCategories();
    }

    @Override
    public long saveCategory(ProductCategory productCategory) {
        if (productCategory.getId() <= 0L) {
            productCategory.setToNew();
        }
        return productCategoryDao.saveCategory(productCategory);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void removeCategory(long categoryId) {
        productCategoryDao.removeCategoryGroups(categoryId);
        productCategoryDao.removeCategory(categoryId);
    }
}

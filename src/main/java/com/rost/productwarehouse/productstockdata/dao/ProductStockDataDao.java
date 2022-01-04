package com.rost.productwarehouse.productstockdata.dao;

import com.rost.productwarehouse.productstockdata.ProductStockData;

import java.util.Collection;
import java.util.Map;

public interface ProductStockDataDao {

    Map<Long, ProductStockData> getProductsStockData(Collection<Long> productsIds);

    Map<Long, ProductStockData> getProductsStockDataLessValue(int value);

    void saveProductsStockData(Collection<ProductStockData> productsStockData);

    void updateProductsStockData(Collection<ProductStockData> productsStockData);
}

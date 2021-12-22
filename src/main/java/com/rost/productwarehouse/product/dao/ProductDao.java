package com.rost.productwarehouse.product.dao;

import com.rost.productwarehouse.product.Product;

import java.util.List;

public interface ProductDao {

    List<Product> getProducts(List<Long> productsIds);

    long storeProduct(Product product);

    void deleteProduct(long productId);
}

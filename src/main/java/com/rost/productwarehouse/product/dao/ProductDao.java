package com.rost.productwarehouse.product.dao;

import com.rost.productwarehouse.product.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductDao {

    List<Product> getProducts();

    Map<Long, Product> getProducts(Collection<Long> productsIds);

    long storeProduct(Product product);

    void deleteProduct(long productId);

    void deleteProductFromGroup(long productId);
}

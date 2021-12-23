package com.rost.productwarehouse.product.service;

import com.rost.productwarehouse.product.Product;

import java.util.List;

public interface ProductService {

    List<Product> getProducts();

    List<Product> getDecoratedProducts(List<Long> productsIds);

    Product getDecoratedProduct(long productId);

    long storeProduct(Product product);

    void deleteProduct(long productId);
}

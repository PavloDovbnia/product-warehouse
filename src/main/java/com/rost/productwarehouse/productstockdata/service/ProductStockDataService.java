package com.rost.productwarehouse.productstockdata.service;

import com.rost.productwarehouse.order.Order;
import com.rost.productwarehouse.productstockdata.ProductStockData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductStockDataService {

    int DEFAULT_LIMIT_STOCK_VALUE = 5;

    int DEFAULT_PRODUCT_ORDERING_VALUE = 20;

    List<ProductStockData> getDecoratedProductsStockData();

    Map<Long, ProductStockData> getGroupProductsStockData(long groupId);

    Map<Long, ProductStockData> getProductsStockData(List<Long> productsIds);

    Map<Long, ProductStockData> getProductsStockDataLessValue(int value);

    void saveProductsStockData(Collection<ProductStockData> productsStockData);

    void handleProvidingOrders(Collection<Order> orders);

    Collection<Order> handleConsumingOrders(Collection<Order> orders);

    void handleCancelledOrders(Collection<Order> orders);

    boolean canHandleConsumingOrder(Order order);
}

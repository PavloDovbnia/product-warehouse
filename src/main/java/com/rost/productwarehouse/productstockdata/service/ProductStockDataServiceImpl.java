package com.rost.productwarehouse.productstockdata.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rost.productwarehouse.order.Order;
import com.rost.productwarehouse.order.OrderData;
import com.rost.productwarehouse.productgroup.ProductGroup;
import com.rost.productwarehouse.productgroup.dao.ProductGroupDao;
import com.rost.productwarehouse.productstockdata.ProductStockData;
import com.rost.productwarehouse.productstockdata.dao.ProductStockDataDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductStockDataServiceImpl implements ProductStockDataService {

    private final ProductStockDataDao productStockDataDao;
    private final ProductGroupDao productGroupDao;

    public ProductStockDataServiceImpl(ProductStockDataDao productStockDataDao, ProductGroupDao productGroupDao) {
        this.productStockDataDao = productStockDataDao;
        this.productGroupDao = productGroupDao;
    }

    @Override
    public Map<Long, ProductStockData> getGroupProductsStockData(long groupId) {
        List<ProductGroup> groups = productGroupDao.getGroups(Lists.newArrayList(groupId));
        if (CollectionUtils.isNotEmpty(groups)) {
            ProductGroup group = groups.iterator().next();
            return productStockDataDao.getProductsStockData(group.getProductsIds());
        }
        return Maps.newHashMap();
    }

    @Override
    public Map<Long, ProductStockData> getProductsStockData(List<Long> productsIds) {
        return productStockDataDao.getProductsStockData(productsIds);
    }

    @Override
    public Map<Long, ProductStockData> getProductsStockDataLessValue(int value) {
        return productStockDataDao.getProductsStockDataLessValue(value);
    }

    @Override
    public void saveProductsStockData(Collection<ProductStockData> productsStockData) {
        productStockDataDao.saveProductsStockData(productsStockData);
    }

    @Override
    public void handleProvidingOrders(Collection<Order> orders) {
        Map<Long, ProductStockData> productsStockData = Maps.newTreeMap();

        orders.stream().filter(order -> Order.Type.PROVIDING.equals(order.getType())).forEach(order -> order.getRows().forEach(row -> {
            ProductStockData stockData = productsStockData.get(row.getProductId());
            if (stockData == null) {
                productsStockData.put(row.getProductId(), new ProductStockData(row.getProductId(), row.getValue()));
            } else {
                stockData.setStockValue(stockData.getStockValue() + row.getValue());
            }
        }));

        productStockDataDao.updateProductsStockData(productsStockData.values());
    }

    @Override
    public Collection<Order> handleConsumingOrders(Collection<Order> orders) {
        Map<Long, ProductStockData> productsStockData = Maps.newTreeMap();

        List<Long> productsIds = orders.stream().filter(order -> Order.Type.CONSUMING.equals(order.getType()))
                .flatMap(order -> order.getRows().stream()).map(OrderData::getProductId).collect(Collectors.toList());
        Map<Long, ProductStockData> existsProductsStockData = productStockDataDao.getProductsStockData(productsIds);

        Set<Order> ordersCanNotBeHandled = Sets.newLinkedHashSet();
        for (Order order : orders) {
            for (OrderData row : order.getRows()) {
                ProductStockData existsProductStockData = existsProductsStockData.get(row.getProductId());
                if (existsProductStockData != null && existsProductStockData.getStockValue() >= row.getValue()) {
                    ProductStockData stockData = productsStockData.get(row.getProductId());
                    if (stockData == null) {
                        productsStockData.put(row.getProductId(), new ProductStockData(row.getProductId(), (-1) * row.getValue()));
                    } else {
                        stockData.setStockValue(stockData.getStockValue() - row.getValue());
                    }
                } else {
                    ordersCanNotBeHandled.add(order);
                    break;
                }
            }
            if (ordersCanNotBeHandled.contains(order)) {
                order.getRows().forEach(row -> {
                    ProductStockData existsProductStockData = existsProductsStockData.get(row.getProductId());
                    existsProductStockData.setStockValue(existsProductStockData.getStockValue() - row.getValue());
                });
            }
        }
        productStockDataDao.updateProductsStockData(productsStockData.values());
        return ordersCanNotBeHandled;
    }

    @Override
    public void handleCancelledOrders(Collection<Order> orders) {
        Map<Long, Integer> returnedProducts = orders.stream().flatMap(order -> order.getRows().stream()).collect(Collectors.toMap(OrderData::getProductId, OrderData::getValue));
        Map<Long, ProductStockData> productsStockData = productStockDataDao.getProductsStockData(returnedProducts.keySet());
        productsStockData.forEach((productId, productStockData) -> {
            int returnedValue = returnedProducts.getOrDefault(productId, 0);
            productStockData.setStockValue(productStockData.getStockValue() + returnedValue);
        });
        productStockDataDao.updateProductsStockData(productsStockData.values());
    }

    @Override
    public boolean canHandleConsumingOrder(Order order) {
        List<Long> productsIds = order.getRows().stream().map(OrderData::getProductId).collect(Collectors.toList());
        Map<Long, ProductStockData> existsProductsStockData = productStockDataDao.getProductsStockData(productsIds);
        for (OrderData row : order.getRows()) {
            ProductStockData existsProductStockData = existsProductsStockData.get(row.getProductId());
            if (existsProductStockData == null || existsProductStockData.getStockValue() < row.getValue()) {
                return false;
            }
        }
        return true;
    }
}

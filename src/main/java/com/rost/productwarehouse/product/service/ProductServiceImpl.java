package com.rost.productwarehouse.product.service;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.itemproperty.service.ItemPropertyService;
import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.product.dao.ProductDao;
import com.rost.productwarehouse.productstockdata.ProductStockData;
import com.rost.productwarehouse.productstockdata.service.ProductStockDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;
    private final ItemPropertyService itemPropertyService;
    private final ProductStockDataService productStockDataService;

    public ProductServiceImpl(ProductDao productDao, ItemPropertyService itemPropertyService, ProductStockDataService productStockDataService) {
        this.productDao = productDao;
        this.itemPropertyService = itemPropertyService;
        this.productStockDataService = productStockDataService;
    }

    @Override
    public List<Product> getProducts() {
        return productDao.getProducts();
    }

    @Override
    public List<Product> getDecoratedProducts(List<Long> productsIds) {
        return decorateProducts(Lists.newArrayList(productDao.getProducts(productsIds).values()));
    }

    @Override
    public Product getDecoratedProduct(long productId) {
        List<Product> products = decorateProducts(Lists.newArrayList(productDao.getProducts(Lists.newArrayList(productId)).values()));
        return CollectionUtils.isNotEmpty(products) ? products.iterator().next() : null;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public long storeProduct(Product product) {
        if (product.getId() <= 0L) {
            product.setToNew();
        }
        boolean isNew = product.isNew();
        long productId = productDao.storeProduct(product);
        product.setId(productId);

        if (isNew) {
            ProductStockData productStockData = new ProductStockData();
            productStockData.setProductId(productId);
            productStockDataService.saveProductsStockData(Lists.newArrayList(productStockData));
        }

        if (MapUtils.isNotEmpty(product.getProperties().getProperties())) {
            itemPropertyService.saveItemValues(productId, product.getProperties().getProperties(), ItemLevel.PRODUCT);
        }
        return productId;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteProduct(long productId) {
        productDao.deleteProduct(productId);
    }

    private List<Product> decorateProducts(List<Product> products) {
        if (CollectionUtils.isNotEmpty(products)) {
            List<Long> productsIds = products.stream().map(Product::getId).collect(Collectors.toList());
            Map<Long, ItemPropertiesHolder> productsProperties = itemPropertyService.getPropertiesValues(productsIds, ItemLevel.PRODUCT);

            products.forEach(product -> {
                ItemPropertiesHolder holder = productsProperties.get(product.getId());
                if (holder != null) {
                    product.getProperties().addProperties(holder.getProperties());
                }
            });
        }
        return products;
    }
}

package com.rost.productwarehouse.productprovider.service;

import com.rost.productwarehouse.productprovider.ProductProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductProviderService {

    Map<Long, ProductProvider> getProviders();

    Map<Long, List<ProductProvider>> getDecoratedProductProviders(Collection<Long> productsIds);

    Map<Long, ProductProvider> getDecoratedProviders(Collection<Long> providersIds);

    void saveProvider(ProductProvider provider);

    void deleteProvider(long providerId);
}

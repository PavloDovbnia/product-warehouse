package com.rost.productwarehouse.productprovider.dao;

import com.rost.productwarehouse.productprovider.ProductProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProductProviderDao {

    Map<Long, ProductProvider> getProviders();

    Map<Long, List<ProductProvider>> getProductProviders(Collection<Long> productsIds);

    Map<Long, ProductProvider> getProviders(Collection<Long> providersIds);

    void saveProviders(Collection<ProductProvider> providers);

    void deleteProviders(Collection<Long> providersIds);
}

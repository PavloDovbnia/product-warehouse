package com.rost.productwarehouse.productprovider.service;

import com.rost.productwarehouse.productprovider.ProductProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductProviderService {

    Map<Long, ProductProvider> getProviders();

    Map<Long, List<ProductProvider>> getDecoratedProductProviders(Collection<Long> productsIds);

    Map<Long, ProductProvider> getDecoratedProviders(Collection<Long> providersIds);

    void saveProviders(Collection<ProductProvider> providers);

    void saveProductProviders(long productId, Set<Long> providersToAdd, Set<Long> providersToDelete);

    void deleteProviders(Collection<Long> providersIds);
}

package com.rost.productwarehouse.productprovider.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.product.dao.ProductDao;
import com.rost.productwarehouse.productprovider.ProductProvider;
import com.rost.productwarehouse.productprovider.dao.ProductProviderDao;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.UserDao;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductProviderServiceImpl implements ProductProviderService {

    private final ProductProviderDao productProviderDao;
    private final ProductDao productDao;
    private final UserDao userDao;

    public ProductProviderServiceImpl(ProductProviderDao productProviderDao, ProductDao productDao, UserDao userDao) {
        this.productProviderDao = productProviderDao;
        this.productDao = productDao;
        this.userDao = userDao;
    }

    @Override
    public Map<Long, ProductProvider> getProviders() {
        return productProviderDao.getProviders();
    }

    @Override
    public Map<Long, List<ProductProvider>> getDecoratedProductProviders(Collection<Long> productsIds) {
        return decorateProductsProviders(productProviderDao.getProductProviders(productsIds));
    }

    @Override
    public Map<Long, ProductProvider> getDecoratedProviders(Collection<Long> providersIds) {
        return decorateProviders(productProviderDao.getProviders(providersIds));
    }

    @Override
    public void saveProvider(ProductProvider provider) {
        productProviderDao.saveProvider(provider);
    }

    @Override
    public void deleteProvider(long providerId) {
        productProviderDao.deleteProvider(providerId);
    }

    private Map<Long, ProductProvider> decorateProviders(Map<Long, ProductProvider> providers) {
        if (MapUtils.isNotEmpty(providers)) {
            Map<Long, User> users = userDao.getUsers(providers.keySet());

            List<Long> productsIds = providers.values().stream().flatMap(provider -> provider.getProductsIds().stream()).collect(Collectors.toList());
            Map<Long, Product> mappedProducts = productDao.getProducts(productsIds);

            return providers.entrySet().stream()
                    .peek(entry -> decorateProvider(entry.getValue(), users, mappedProducts))
                    .filter(entry -> entry.getValue().getProvider() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, Maps::newTreeMap));
        }
        return providers;
    }

    private Map<Long, List<ProductProvider>> decorateProductsProviders(Map<Long, List<ProductProvider>> productsProviders) {
        if (MapUtils.isNotEmpty(productsProviders)) {
            List<Long> providersIds = productsProviders.values().stream().flatMap(List::stream).map(ProductProvider::getProviderId).collect(Collectors.toList());
            Map<Long, User> users = userDao.getUsers(providersIds);
            Map<Long, Product> mappedProducts = productDao.getProducts(productsProviders.keySet());

            return productsProviders.entrySet().stream().peek(entry -> {
                List<ProductProvider> preparedProviders = entry.getValue().stream().peek(provider -> {
                    decorateProvider(provider, users, mappedProducts);
                }).filter(provider -> provider.getProvider() != null).collect(Collectors.toList());
                entry.setValue(preparedProviders);
            }).filter(entry -> !entry.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        }
        return productsProviders;
    }

    private boolean isUserProvider(User user) {
        return user != null && user.getRoles().stream().map(Role::getType).collect(Collectors.toSet()).contains(Role.Type.ROLE_PRODUCT_PROVIDER);
    }

    private ProductProvider decorateProvider(ProductProvider provider, Map<Long, User> users, Map<Long, Product> mappedProducts) {
        User user = users.get(provider.getProviderId());
        if (isUserProvider(user)) {
            provider.setProvider(user);

            List<Product> products = Lists.newArrayList();
            provider.getProductsIds().forEach(productId -> {
                Product product = mappedProducts.get(productId);
                if (product != null) {
                    products.add(product);
                }
            });
            provider.setProducts(products);
        }
        return provider;
    }
}

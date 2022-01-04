package com.rost.productwarehouse.productprovider;

import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.security.User;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProductProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    private long providerId;
    private User provider;
    private Set<Long> productsIds;
    private List<Product> products;

    public long getProviderId() {
        return providerId;
    }

    public void setProviderId(long providerId) {
        this.providerId = providerId;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public Set<Long> getProductsIds() {
        return productsIds;
    }

    public void setProductsIds(Set<Long> productsIds) {
        this.productsIds = productsIds;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductProvider that = (ProductProvider) o;
        return providerId == that.providerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId);
    }

    @Override
    public String toString() {
        return "ProductProvider{" +
                "providerId=" + providerId +
                ", provider=" + provider +
                ", productsIds=" + productsIds +
                ", products=" + products +
                '}';
    }
}

package com.rost.productwarehouse.productprovider;

import java.util.List;

public class ProductProvidersDto {

    private long productId;
    private List<Long> providersIdsToAdd;
    private List<Long> providersIdsToDelete;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public List<Long> getProvidersIdsToAdd() {
        return providersIdsToAdd;
    }

    public void setProvidersIdsToAdd(List<Long> providersIdsToAdd) {
        this.providersIdsToAdd = providersIdsToAdd;
    }

    public List<Long> getProvidersIdsToDelete() {
        return providersIdsToDelete;
    }

    public void setProvidersIdsToDelete(List<Long> providersIdsToDelete) {
        this.providersIdsToDelete = providersIdsToDelete;
    }
}

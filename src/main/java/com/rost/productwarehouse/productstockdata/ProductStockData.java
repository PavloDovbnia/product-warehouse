package com.rost.productwarehouse.productstockdata;

import java.io.Serializable;
import java.util.Objects;

public class ProductStockData implements Serializable {

    private static final long serialVersionUID = 1L;

    private long productId;
    private int stockValue;

    public ProductStockData() {
    }

    public ProductStockData(long productId, int stockValue) {
        this.productId = productId;
        this.stockValue = stockValue;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getStockValue() {
        return stockValue;
    }

    public void setStockValue(int stockValue) {
        this.stockValue = stockValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductStockData that = (ProductStockData) o;
        return productId == that.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    @Override
    public String toString() {
        return "ProductStockData{" +
                "productId=" + productId +
                ", stockValue=" + stockValue +
                '}';
    }
}

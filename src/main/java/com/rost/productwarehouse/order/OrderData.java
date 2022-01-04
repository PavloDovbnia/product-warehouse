package com.rost.productwarehouse.order;

import com.rost.productwarehouse.product.Product;

import java.io.Serializable;
import java.util.Objects;

public class OrderData implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long orderId;
    private long productId;
    private Product product;
    private int value;

    public OrderData() {
    }

    public OrderData(long productId, int value) {
        this.productId = productId;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderData orderData = (OrderData) o;
        return id == orderData.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

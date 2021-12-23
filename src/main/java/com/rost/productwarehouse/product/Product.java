package com.rost.productwarehouse.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.productgroup.ProductGroup;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private ProductGroup productGroup;
    private ItemPropertiesHolder properties = new ItemPropertiesHolder();

    public Product() {
    }

    public Product(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }

    public ItemPropertiesHolder getProperties() {
        return properties;
    }

    @JsonIgnore
    public boolean isNew() {
        return id == 0;
    }

    public void setToNew() {
        this.id = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", productGroup=" + productGroup +
                ", properties=" + properties +
                '}';
    }
}

package com.rost.productwarehouse.productgroup;

import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.productcategory.ProductCategory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class ProductGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private ProductCategory category;
    private ItemPropertiesHolder properties = new ItemPropertiesHolder();
    private List<Product> products;

    public ProductGroup() {
    }

    public ProductGroup(long id, String name) {
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ItemPropertiesHolder getProperties() {
        return properties;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public boolean isNew() {
        return id == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductGroup that = (ProductGroup) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", properties=" + properties +
                ", products=" + products +
                '}';
    }
}

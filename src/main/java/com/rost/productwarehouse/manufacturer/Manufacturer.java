package com.rost.productwarehouse.manufacturer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rost.productwarehouse.itemproperty.ItemPropertiesHolder;
import com.rost.productwarehouse.productgroup.ProductGroup;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Manufacturer implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private ItemPropertiesHolder properties = new ItemPropertiesHolder();
    private List<ProductGroup> productGroups;
    private Set<Long> productGroupsIds;

    public Manufacturer() {
    }

    public Manufacturer(long id, String name) {
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

    public ItemPropertiesHolder getProperties() {
        return properties;
    }

    public List<ProductGroup> getProductGroups() {
        return productGroups;
    }

    public void setProductGroups(List<ProductGroup> productGroups) {
        this.productGroups = productGroups;
    }

    @JsonIgnore
    public boolean isNew() {
        return 0 == id;
    }

    public void setToNew() {
        this.id = 0L;
    }

    public Set<Long> getProductGroupsIds() {
        return productGroupsIds;
    }

    public void setProductGroupsIds(Set<Long> productGroupsIds) {
        this.productGroupsIds = productGroupsIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Manufacturer that = (Manufacturer) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Manufacturer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", properties=" + properties +
                ", productGroups=" + productGroups +
                ", productGroupsIds=" + productGroupsIds +
                '}';
    }
}

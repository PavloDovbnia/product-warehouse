package com.rost.productwarehouse.itemproperty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Objects;

public class ItemProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String token;
    private String name;
    private ItemPropertyValueType type;
    private ItemPropertyValueDataType dataType;
    private ItemLevel itemLevel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemPropertyValueType getType() {
        return type;
    }

    public void setType(ItemPropertyValueType type) {
        this.type = type;
    }

    public ItemPropertyValueDataType getDataType() {
        return dataType;
    }

    public void setDataType(ItemPropertyValueDataType dataType) {
        this.dataType = dataType;
    }

    public ItemLevel getItemLevel() {
        return itemLevel;
    }

    public void setItemLevel(ItemLevel itemLevel) {
        this.itemLevel = itemLevel;
    }

    @JsonIgnore
    public boolean isNew() {
        return getId() == 0L;
    }

    public void setToNew() {
        this.id = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemProperty that = (ItemProperty) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemProperty{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", dataType=" + dataType +
                ", itemLevel=" + itemLevel +
                '}';
    }
}

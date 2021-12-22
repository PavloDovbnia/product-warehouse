package com.rost.productwarehouse.menu;

import java.io.Serializable;
import java.util.Objects;

public class MenuItemsGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItemsGroup that = (MenuItemsGroup) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItemsGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

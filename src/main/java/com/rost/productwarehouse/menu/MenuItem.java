package com.rost.productwarehouse.menu;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class MenuItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum AccessType {
        READ_ONLY, READ_WRITE;

        public static AccessType of(String type) {
            return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(type)).findFirst().orElse(READ_ONLY);
        }
    }

    private long id;
    private String name;
    private String url;
    private AccessType accessType;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", accessType=" + accessType +
                '}';
    }
}

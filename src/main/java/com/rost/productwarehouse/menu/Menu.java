package com.rost.productwarehouse.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.security.Role;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Menu implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Role role;
    private Map<MenuItemsGroup, List<MenuItem>> items = Maps.newLinkedHashMap();

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<MenuItemsGroup, List<MenuItem>> getItems() {
        return items;
    }

    public void setItems(Map<MenuItemsGroup, List<MenuItem>> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return Objects.equals(role, menu.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }

    @Override
    public String toString() {
        return "Menu{" +
                "role=" + role +
                ", items=" + items +
                '}';
    }
}

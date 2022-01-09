package com.rost.productwarehouse.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        ROLE_MANAGER("MANAGER", 1), ROLE_PRODUCT_PROVIDER("PRODUCT_PROVIDER", 2), ROLE_PRODUCT_CONSUMER("PRODUCT_CONSUMER", 3), ROLE_ADMIN("ADMIN", 0);

        private String name;
        private int sort;

        Type(String name, int sort) {
            this.name = name;
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public int getSort() {
            return sort;
        }

        public static Optional<Type> of(String type) {
            return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(type)).findFirst();
        }

        public static String[] names(Type... types) {
            return Arrays.stream(types).map(Type::getName).toArray(String[]::new);
        }

        public static String[] names() {
            return Arrays.stream(values()).map(Type::getName).toArray(String[]::new);
        }
    }

    private long id;
    private Type type;

    public Role() {
    }

    public Role(Type type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id == role.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", type=" + type +
                '}';
    }
}

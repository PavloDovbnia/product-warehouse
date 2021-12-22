package com.rost.productwarehouse.security;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class PasswordResetToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private long userId;
    private LocalDateTime created;

    public PasswordResetToken() {
    }

    public PasswordResetToken(String token, long userId) {
        this.token = token;
        this.userId = userId;
    }

    public PasswordResetToken(String token, long userId, LocalDateTime created) {
        this.token = token;
        this.userId = userId;
        this.created = created;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return userId == that.userId &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, userId);
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "token='" + token + '\'' +
                ", userId=" + userId +
                ", created=" + created +
                '}';
    }
}

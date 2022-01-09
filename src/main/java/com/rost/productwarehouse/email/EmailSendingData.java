package com.rost.productwarehouse.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rost.productwarehouse.security.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class EmailSendingData implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        NOT_SENT, SENT, ERROR, BROKEN_DATA
    }

    public enum Type {
        PASSWORD_RESET_TOKEN, ORDER_CAN_NOT_BE_HANDLED, PROVIDING_ORDER_REQUESTED, CONSUMING_ORDER_REQUESTED, CANCELLED_PROVIDING_ORDER,
        USER_REGISTERED
    }

    private long id;
    private String email;
    private User user;
    private Type type;
    private Status status;
    private Map<String, Object> data;
    private LocalDateTime created;
    private LocalDateTime sendingDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getSendingDate() {
        return sendingDate;
    }

    public void setSendingDate(LocalDateTime sendingDate) {
        this.sendingDate = sendingDate;
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
        EmailSendingData that = (EmailSendingData) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmailSendingData{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", user=" + user +
                ", type=" + type +
                ", status=" + status +
                ", data=" + data +
                ", created=" + created +
                ", sendingDate=" + sendingDate +
                '}';
    }
}

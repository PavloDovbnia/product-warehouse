package com.rost.productwarehouse.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rost.productwarehouse.security.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        PROVIDING, CONSUMING;
    }

    public enum State {
        REQUESTED, HANDLING, SHIPPING, DELIVERED, COMPLETED, CANCELLED, CANCELLED_COMPLETED, CAN_NOT_BE_HANDLED;

        public boolean isOneOf(State... states) {
            return Arrays.asList(states).contains(this);
        }
    }

    private long id;
    private Type type;
    private long userId;
    private User user;
    private LocalDateTime created;
    private State state = State.REQUESTED;
    private LocalDateTime stateChanged;
    private String stateComment;
    private List<OrderData> rows;

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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public LocalDateTime getStateChanged() {
        return stateChanged;
    }

    public void setStateChanged(LocalDateTime stateChanged) {
        this.stateChanged = stateChanged;
    }

    public String getStateComment() {
        return stateComment;
    }

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public List<OrderData> getRows() {
        return rows;
    }

    public void setRows(List<OrderData> rows) {
        this.rows = rows;
    }

    @JsonIgnore
    public boolean isNew() {
        return getId() <= 0L;
    }

    public void setToNew() {
        this.id = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", type=" + type +
                ", userId=" + userId +
                ", user=" + user +
                ", created=" + created +
                ", state=" + state +
                ", stateChanged=" + stateChanged +
                ", stateComment='" + stateComment + '\'' +
                ", rows=" + rows +
                '}';
    }
}

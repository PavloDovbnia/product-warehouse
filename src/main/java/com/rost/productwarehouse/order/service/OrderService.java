package com.rost.productwarehouse.order.service;

import com.rost.productwarehouse.order.Order;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderService {

    List<Order> getOrders(Order.Type type, LocalDateTime fromCreated);

    List<Order> getOrders(Order.Type type, Collection<Order.State> states);

    List<Order> getOrders(Order.Type type, long userId, LocalDateTime fromCreated);

    Order getOrder(long orderId);

    long saveOrder(Order order);

    void updateOrderState(long orderId, Order.State state, String stateComment);

    void handleProvidingOrders(Collection<Order> orders);

    Collection<Order> handleConsumingOrders(Collection<Order> orders);

    void handleCancelledOrders(Collection<Order> orders);
}

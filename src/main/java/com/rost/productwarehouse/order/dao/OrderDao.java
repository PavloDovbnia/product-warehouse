package com.rost.productwarehouse.order.dao;

import com.rost.productwarehouse.order.Order;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderDao {

    List<Order> getOrders(Order.Type type, LocalDateTime fromCreated);

    List<Order> getOrders(Order.Type type, Collection<Order.State> states);

    List<Order> getOrders(Order.Type type, long userId, LocalDateTime fromCreated);

    Order getOrder(long orderId);

    long saveOrder(Order order);

    void updateOrdersState(Collection<Long> ordersIds, Order.State state, String stateComment);
}

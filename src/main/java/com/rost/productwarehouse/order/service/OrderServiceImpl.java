package com.rost.productwarehouse.order.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.order.Order;
import com.rost.productwarehouse.order.OrderData;
import com.rost.productwarehouse.order.dao.OrderDao;
import com.rost.productwarehouse.product.Product;
import com.rost.productwarehouse.product.dao.ProductDao;
import com.rost.productwarehouse.productstockdata.service.ProductStockDataService;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.UserDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final UserDao userDao;
    private final ProductStockDataService productStockDataService;

    public OrderServiceImpl(OrderDao orderDao, ProductDao productDao, UserDao userDao, ProductStockDataService productStockDataService) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.userDao = userDao;
        this.productStockDataService = productStockDataService;
    }

    @Override
    public List<Order> getOrders(Order.Type type, LocalDateTime fromCreated) {
        return decorate(orderDao.getOrders(type, fromCreated));
    }

    @Override
    public List<Order> getOrders(Order.Type type, Collection<Order.State> states) {
        return decorate(orderDao.getOrders(type, states));
    }

    @Override
    public List<Order> getOrders(Order.Type type, long userId, LocalDateTime fromCreated) {
        return decorate(orderDao.getOrders(type, userId, fromCreated));
    }

    @Override
    public Order getOrder(long orderId) {
        return decorate(orderDao.getOrder(orderId));
    }

    @Override
    public long saveOrder(Order order) {
        return orderDao.saveOrder(order);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void updateOrderState(long orderId, Order.State state, String stateComment) {
        Order storedOrder = orderDao.getOrder(orderId);
        if (storedOrder.getState().isOneOf(Order.State.REQUESTED, Order.State.HANDLING, Order.State.SHIPPING, Order.State.DELIVERED)) {
            orderDao.updateOrdersState(Lists.newArrayList(orderId), state, stateComment);

            Order order = orderDao.getOrder(orderId);
            if (order != null) {
                if (Order.State.COMPLETED.equals(order.getState()) && Order.Type.PROVIDING.equals(order.getType())) {
                    productStockDataService.handleProvidingOrders(Lists.newArrayList(order));
                }
            }
        } else {
            throw new RuntimeException("Can not update order state due the current state is " + storedOrder.getState());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void handleProvidingOrders(Collection<Order> orders) {
        orders = orders.stream().filter(order -> Order.State.DELIVERED.equals(order.getState())).collect(Collectors.toList());
        productStockDataService.handleProvidingOrders(orders);
        List<Long> completedOrdersIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        orderDao.updateOrdersState(completedOrdersIds, Order.State.COMPLETED, null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public Collection<Order> handleConsumingOrders(Collection<Order> orders) {
        orders = orders.stream().filter(order -> Order.State.REQUESTED.equals(order.getState())).collect(Collectors.toList());
        Collection<Order> ordersCanNotBeHandled = productStockDataService.handleConsumingOrders(orders);
        List<Long> completedOrdersIds = orders.stream().filter(order -> !ordersCanNotBeHandled.contains(order)).map(Order::getId).collect(Collectors.toList());
        orderDao.updateOrdersState(completedOrdersIds, Order.State.HANDLING, null);
        List<Long> ordersCanNotBeHandledIds = ordersCanNotBeHandled.stream().map(Order::getId).collect(Collectors.toList());
        orderDao.updateOrdersState(ordersCanNotBeHandledIds, Order.State.CAN_NOT_BE_HANDLED, "Not enough products");
        return ordersCanNotBeHandled;
    }

    @Override
    public void handleCancelledOrders(Collection<Order> orders) {
        orders = orders.stream().filter(order -> Order.Type.CONSUMING.equals(order.getType()) && Order.State.CANCELLED.equals(order.getState())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(orders)) {
            productStockDataService.handleCancelledOrders(orders);
            List<Long> ordersIds = orders.stream().map(Order::getId).collect(Collectors.toList());
            orderDao.updateOrdersState(ordersIds, Order.State.CANCELLED_COMPLETED, null);
        }
    }

    private List<Order> decorate(List<Order> orders) {
        if (CollectionUtils.isNotEmpty(orders)) {
            List<Long> usersIds = orders.stream().map(Order::getUserId).collect(Collectors.toList());
            List<Long> productsIds = orders.stream().flatMap(order -> order.getRows().stream()).map(OrderData::getProductId).collect(Collectors.toList());

            Map<Long, User> users = userDao.getUsers(usersIds);
            Map<Long, Product> products = productDao.getProducts(productsIds);

            orders.forEach(order -> {
                order.setUser(users.get(order.getUserId()));
                order.getRows().forEach(row -> row.setProduct(products.get(row.getProductId())));
            });
            return orders;
        }
        return orders;
    }

    private Order decorate(Order order) {
        if (order != null) {
            List<Long> productsIds = order.getRows().stream().map(OrderData::getProductId).collect(Collectors.toList());
            Map<Long, User> users = userDao.getUsers(Lists.newArrayList(order.getUserId()));
            Map<Long, Product> products = productDao.getProducts(productsIds);

            order.setUser(users.get(order.getUserId()));
            order.getRows().forEach(row -> row.setProduct(products.get(row.getProductId())));
            return order;
        }
        return null;
    }

    private Order joinAllIdenticalRows(Order order) {
        Map<Long, OrderData> joinedRows = Maps.newLinkedHashMap();
        order.getRows().forEach(row -> {
            OrderData joinedRow = joinedRows.get(row.getProductId());
            if (joinedRow == null) {
                joinedRows.put(row.getProductId(), row);
            } else {
                joinedRow.setValue(joinedRow.getValue() + row.getValue());
            }
        });
        order.setRows(Lists.newArrayList(joinedRows.values()));
        return order;
    }
}

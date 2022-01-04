package com.rost.productwarehouse.scheduling;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.service.EmailSendingDataService;
import com.rost.productwarehouse.order.Order;
import com.rost.productwarehouse.order.OrderData;
import com.rost.productwarehouse.order.service.OrderService;
import com.rost.productwarehouse.productprovider.ProductProvider;
import com.rost.productwarehouse.productprovider.service.ProductProviderService;
import com.rost.productwarehouse.productstockdata.ProductStockData;
import com.rost.productwarehouse.productstockdata.service.ProductStockDataService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrdersHandlingTask {

    private static final Logger LOG = LoggerFactory.getLogger(OrdersHandlingTask.class);

    private final EmailSendingDataService emailSendingDataService;
    private final OrderService orderService;
    private final ProductStockDataService productStockDataService;
    private final ProductProviderService productProviderService;

    public OrdersHandlingTask(EmailSendingDataService emailSendingDataService, OrderService orderService, ProductStockDataService productStockDataService, ProductProviderService productProviderService) {
        this.emailSendingDataService = emailSendingDataService;
        this.orderService = orderService;
        this.productStockDataService = productStockDataService;
        this.productProviderService = productProviderService;
    }

    @Scheduled(fixedDelay = 60 * 1000)
    public void handleOrders() {
        long start = System.currentTimeMillis();
        LOG.info("OrdersHandlingTask has been started");
        handleCancelledOrders();
        handleProvidingOrders();
        handleConsumingOrders();
        createNewProvidingOrders();
        LOG.info("OrdersHandlingTask has been ended in {} ms", System.currentTimeMillis() - start);
    }

    private void handleCancelledOrders() {
        List<Order> cancelledConsumingOrders = orderService.getOrders(Order.Type.CONSUMING, Lists.newArrayList(Order.State.CANCELLED));
        orderService.handleCancelledOrders(cancelledConsumingOrders);
        List<Order> cancelledProvidingOrders = orderService.getOrders(Order.Type.PROVIDING, Lists.newArrayList(Order.State.CANCELLED));
        List<EmailSendingData> emailSendingDataCancelledProvidingOrders = cancelledProvidingOrders.stream().map(this::createEmailSendingDataCancelledProvidingOrders).collect(Collectors.toList());
        emailSendingDataService.save(emailSendingDataCancelledProvidingOrders);
    }

    private void createNewProvidingOrders() {
        Map<Long, ProductStockData> productsStockData = getProductsToRequestForProviding();
        Map<ProductProvider, List<ProductStockData>> providersToStockData = getProvidersToProducts(productsStockData);
        List<Order> orders = createProvidingOrders(providersToStockData);

        orders.forEach(orderService::saveOrder);
        List<EmailSendingData> emailsSendingData = orders.stream().map(this::createEmailSendingDataOrderRequested).collect(Collectors.toList());
        emailSendingDataService.save(emailsSendingData);
    }

    private List<Order> createProvidingOrders(Map<ProductProvider, List<ProductStockData>> providersToStockData) {
        return providersToStockData.entrySet().stream().map(entry -> {
            ProductProvider provider = entry.getKey();
            List<ProductStockData> products = entry.getValue();

            Order order = new Order();
            order.setType(Order.Type.PROVIDING);
            order.setUserId(provider.getProviderId());
            order.setUser(provider.getProvider());
            order.setRows(products.stream().map(p -> new OrderData(p.getProductId(), ProductStockDataService.DEFAULT_PRODUCT_ORDERING_VALUE)).collect(Collectors.toList()));
            return order;
        }).collect(Collectors.toList());
    }

    private Map<Long, ProductStockData> getProductsToRequestForProviding() {
        Map<Long, ProductStockData> productsStockData = productStockDataService.getProductsStockDataLessValue(ProductStockDataService.DEFAULT_LIMIT_STOCK_VALUE);
        Set<Long> providingProductsInProgress = orderService.getOrders(Order.Type.PROVIDING, Lists.newArrayList(Order.State.REQUESTED, Order.State.HANDLING, Order.State.SHIPPING))
                .stream().flatMap(order -> order.getRows().stream()).map(OrderData::getProductId).collect(Collectors.toSet());

        return productsStockData.entrySet().stream().filter(entry -> !providingProductsInProgress.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, Maps::newLinkedHashMap));
    }

    private Map<ProductProvider, List<ProductStockData>> getProvidersToProducts(Map<Long, ProductStockData> productsToRequestForProviding) {
        Map<Long, List<ProductProvider>> providersMappedByProductId = productProviderService.getDecoratedProductProviders(productsToRequestForProviding.keySet());

        Map<ProductProvider, List<ProductStockData>> providersToStockData = Maps.newHashMap();
        productsToRequestForProviding.forEach((id, productStockData) -> {
            long productId = productStockData.getProductId();
            List<ProductProvider> providers = providersMappedByProductId.get(productId);
            if (CollectionUtils.isNotEmpty(providers)) {
                ProductProvider provider = providers.iterator().next();
                providersToStockData.computeIfAbsent(provider, key -> Lists.newArrayList()).add(productStockData);
            }
        });
        return providersToStockData;
    }

    private void handleProvidingOrders() {
        List<Order> deliveredOrders = orderService.getOrders(Order.Type.PROVIDING, Lists.newArrayList(Order.State.DELIVERED));
        orderService.handleProvidingOrders(deliveredOrders);
    }

    private void handleConsumingOrders() {
        List<Order> requestedOrders = orderService.getOrders(Order.Type.CONSUMING, Lists.newArrayList(Order.State.REQUESTED));
        Collection<Order> ordersCanNotBeHandled = orderService.handleConsumingOrders(requestedOrders);
        if (CollectionUtils.isNotEmpty(ordersCanNotBeHandled)) {
            List<EmailSendingData> emailsData = ordersCanNotBeHandled.stream().map(this::createEmailSendingDataOrderCanNotBeHandled).collect(Collectors.toList());
            List<Long> ordersIds = ordersCanNotBeHandled.stream().map(Order::getId).collect(Collectors.toList());
            LOG.info("Orders can not be handled, not enough products. {}", ordersIds);
            emailSendingDataService.save(emailsData);
        }
    }

    private EmailSendingData createEmailSendingDataOrderCanNotBeHandled(Order order) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(order.getUser().getEmail());
        data.setType(EmailSendingData.Type.ORDER_CAN_NOT_BE_HANDLED);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of(
                "username", order.getUser().getUsername(),
                "topic", "Order " + order.getId() + " can not be handled",
                "view-user-orders-link", "http://localhost:4200/orders/consuming/" + order.getUserId(),
                "order-id", Long.toString(order.getId())));
        return data;
    }

    private EmailSendingData createEmailSendingDataOrderRequested(Order order) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(order.getUser().getEmail());
        data.setType(EmailSendingData.Type.PROVIDING_ORDER_REQUESTED);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of(
                "username", order.getUser().getUsername(),
                "topic", "New Order " + order.getId() + " has been requested",
                "view-user-orders-link", "http://localhost:4200/orders/providing/" + order.getUserId(),
                "order-id", Long.toString(order.getId())));
        return data;
    }

    private EmailSendingData createEmailSendingDataCancelledProvidingOrders(Order order) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(order.getUser().getEmail());
        data.setType(EmailSendingData.Type.CANCELLED_PROVIDING_ORDER);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of(
                "username", order.getUser().getUsername(),
                "topic", "Order " + order.getId() + " has been cancelled",
                "view-user-orders-link", "http://localhost:4200/orders/providing/" + order.getUserId(),
                "order-id", Long.toString(order.getId())));
        return data;
    }
}

package com.rost.productwarehouse.order;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.service.EmailSendingDataService;
import com.rost.productwarehouse.order.service.OrderService;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.UserDao;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order/")
public class OrderController {

    private final OrderService orderService;
    private final EmailSendingDataService emailSendingDataService;
    private final UserDao userDao;

    public OrderController(OrderService orderService, EmailSendingDataService emailSendingDataService, UserDao userDao) {
        this.orderService = orderService;
        this.emailSendingDataService = emailSendingDataService;
        this.userDao = userDao;
    }

    @GetMapping("providing/getFromCreated")
    public ResponseEntity<List<Order>> getProvidingOrdersFromCreated(@RequestParam("fromCreated") String fromCreatedStr) {
        LocalDateTime fromCreated = LocalDate.parse(fromCreatedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        return ResponseEntity.ok(orderService.getOrders(Order.Type.PROVIDING, fromCreated));
    }

    @GetMapping("providing/{order-state}/getByState")
    public ResponseEntity<List<Order>> getProvidingOrdersByState(@PathVariable("order-state") Order.State state) {
        return ResponseEntity.ok(orderService.getOrders(Order.Type.PROVIDING, Lists.newArrayList(state)));
    }

    @GetMapping("providing/{user-id}/get")
    public ResponseEntity<List<Order>> getProvidingUserOrders(@PathVariable("user-id") long userId, @RequestParam("fromCreated") String fromCreatedStr) {
        LocalDateTime fromCreated = LocalDate.parse(fromCreatedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        return ResponseEntity.ok(orderService.getOrders(Order.Type.PROVIDING, userId, fromCreated));
    }

    @GetMapping("consuming/getFromCreated")
    public ResponseEntity<List<Order>> getConsumingOrdersFromCreated(@RequestParam("fromCreated") String fromCreatedStr) {
        LocalDateTime fromCreated = LocalDate.parse(fromCreatedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        return ResponseEntity.ok(orderService.getOrders(Order.Type.CONSUMING, fromCreated));
    }

    @GetMapping("consuming/{order-state}/getByState")
    public ResponseEntity<List<Order>> getConsumingOrdersByState(@PathVariable("order-state") Order.State state) {
        return ResponseEntity.ok(orderService.getOrders(Order.Type.CONSUMING, Lists.newArrayList(state)));
    }

    @GetMapping("consuming/{user-id}/get")
    public ResponseEntity<List<Order>> getConsumingUserOrders(@PathVariable("user-id") long userId, @RequestParam("fromCreated") String fromCreatedStr) {
        LocalDateTime fromCreated = LocalDate.parse(fromCreatedStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        return ResponseEntity.ok(orderService.getOrders(Order.Type.CONSUMING, userId, fromCreated));
    }

    @PostMapping("providing/save")
    public ResponseEntity<Order> saveProvidingOrder(@RequestBody Order order) {
        order.setType(Order.Type.PROVIDING);
        long orderId = orderService.saveOrder(order);
        order = orderService.getOrder(orderId);
        sendEmailProvidingOrderRequested(order);
        return ResponseEntity.ok(order);
    }

    @PostMapping("consuming/save")
    public ResponseEntity<Order> saveConsumingProvidingOrder(@RequestBody Order order) {
        order.setType(Order.Type.CONSUMING);
        long orderId = orderService.saveOrder(order);
        sendEmailConsumingOrderRequested(order);
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PostMapping("updateOrderState")
    public ResponseEntity<Order> updateOrderState(@RequestBody Map<String, String> body) {
        long orderId = Long.parseLong(body.get("orderId"));
        Order.State state = Order.State.valueOf(body.get("state"));
        String comment = body.get("comment");
        orderService.updateOrderState(orderId, state, comment);
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    private void sendEmailConsumingOrderRequested(Order order) {
        List<User> users = userDao.getUsersByRoles(Lists.newArrayList(Role.Type.ROLE_MANAGER));
        if (CollectionUtils.isNotEmpty(users)) {
            User user = users.iterator().next();

            EmailSendingData data = new EmailSendingData();
            data.setEmail(user.getEmail());
            data.setType(EmailSendingData.Type.CONSUMING_ORDER_REQUESTED);
            data.setStatus(EmailSendingData.Status.NOT_SENT);
            data.setData(ImmutableMap.of(
                    "username", user.getUsername(),
                    "topic", "New Order " + order.getId() + " has been requested",
                    "view-consuming-orders-link", "http://localhost:4200/orders/consuming/",
                    "order-id", Long.toString(order.getId())));

            emailSendingDataService.save(Lists.newArrayList(data));
        }
    }

    private void sendEmailProvidingOrderRequested(Order order) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(order.getUser().getEmail());
        data.setType(EmailSendingData.Type.PROVIDING_ORDER_REQUESTED);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of(
                "username", order.getUser().getUsername(),
                "topic", "New Order " + order.getId() + " has been requested",
                "view-user-orders-link", "http://localhost:4200/orders/providing/" + order.getUserId(),
                "order-id", Long.toString(order.getId())));

        emailSendingDataService.save(Lists.newArrayList(data));
    }
}

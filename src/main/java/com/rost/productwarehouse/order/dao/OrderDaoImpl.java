package com.rost.productwarehouse.order.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.order.Order;
import com.rost.productwarehouse.order.OrderData;
import com.rost.productwarehouse.utils.DbUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Order> getOrders(Order.Type type, LocalDateTime fromCreated) {
        String sql = "select o.id order_id, type, user_id, created, state, state_changed, state_comment, d.id order_data_id, product_id, value " +
                "from `order` o, order_data d " +
                "where o.id = d.order_id " +
                "and o.type = :type " +
                "and o.created >= :fromCreated ";
        SqlParameterSource params = new MapSqlParameterSource("type", type.name())
                .addValue("fromCreated", Timestamp.valueOf(fromCreated));
        return jdbcTemplate.query(sql, params, new OrdersExtractor());
    }

    @Override
    public List<Order> getOrders(Order.Type type, Collection<Order.State> states) {
        if (CollectionUtils.isNotEmpty(states)) {
            String sql = "select o.id order_id, type, user_id, created, state, state_changed, state_comment, d.id order_data_id, product_id, value " +
                    "from `order` o, order_data d " +
                    "where o.id = d.order_id " +
                    "and o.type = :type " +
                    "and o.state in (:states) ";
            SqlParameterSource params = new MapSqlParameterSource("type", type.name())
                    .addValue("states", states.stream().map(Order.State::name).collect(Collectors.toList()));
            return jdbcTemplate.query(sql, params, new OrdersExtractor());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<Order> getOrders(Order.Type type, long userId, LocalDateTime fromCreated) {
        String sql = "select o.id order_id, type, user_id, created, state, state_changed, state_comment, d.id order_data_id, product_id, value " +
                "from `order` o, order_data d " +
                "where o.id = d.order_id " +
                "and o.type = :type " +
                "and o.user_id = :userId " +
                "and o.created >= :fromCreated";
        SqlParameterSource params = new MapSqlParameterSource("type", type.name())
                .addValue("userId", userId)
                .addValue("fromCreated", Timestamp.valueOf(fromCreated));
        return jdbcTemplate.query(sql, params, new OrdersExtractor());
    }

    @Override
    public Order getOrder(long orderId) {
        String sql = "select o.id order_id, type, user_id, created, state, state_changed, state_comment, d.id order_data_id, product_id, value " +
                "from `order` o, order_data d " +
                "where o.id = d.order_id " +
                "and o.id = :orderId";
        return DbUtils.extract(jdbcTemplate.query(sql, new MapSqlParameterSource("orderId", orderId), new OrdersExtractor()));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public long saveOrder(Order order) {
        if (CollectionUtils.isNotEmpty(order.getRows())) {
            if (order.isNew()) {
                long orderId = addOrder(order);
                order.setId(orderId);
            } else {
                deleteOrderRows(order);
            }
            addOrderRows(order);
            return order.getId();
        }
        throw new RuntimeException("Order is empty");
    }

    private long addOrder(Order order) {
        String sql = "insert into `order` (type, user_id, state) " +
                "values (:type, :userId, :state)";
        SqlParameterSource params = new MapSqlParameterSource("type", order.getType().name())
                .addValue("userId", order.getUserId())
                .addValue("state", order.getState().name());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void addOrderRows(Order order) {
        String sql = "insert into order_data (order_id, product_id, value) " +
                "values (:orderId, :productId, :value)";
        SqlParameterSource[] batchParams = new MapSqlParameterSource[order.getRows().size()];
        int i = 0;
        for (OrderData row : order.getRows()) {
            batchParams[i] = new MapSqlParameterSource("orderId", order.getId())
                    .addValue("productId", row.getProductId())
                    .addValue("value", row.getValue());
            i++;
        }
        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    private void deleteOrderRows(Order order) {
        String sql = "delete from order_data where order_id = :orderId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("orderId", order.getId()));
    }

    @Override
    public void updateOrdersState(Collection<Long> ordersIds, Order.State state, String stateComment) {
        if (CollectionUtils.isNotEmpty(ordersIds)) {
            String sql = "update `order`" +
                    "set state = :state, " +
                    "state_comment = :stateComment " +
                    "where id in (:ordersIds)";
            SqlParameterSource params = new MapSqlParameterSource("ordersIds", ordersIds)
                    .addValue("state", state.name())
                    .addValue("stateComment", stateComment);
            jdbcTemplate.update(sql, params);
        }
    }

    private static class OrdersExtractor implements ResultSetExtractor<List<Order>> {

        private OrderMapper orderMapper = new OrderMapper();
        private OrderDataMapper orderDataMapper = new OrderDataMapper();

        @Override
        public List<Order> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Order> orders = Maps.newTreeMap();
            while (rs.next()) {
                Order order = orderMapper.mapRow(rs, rs.getRow());
                orders.putIfAbsent(order.getId(), order);
                order = orders.get(order.getId());
                order.getRows().add(orderDataMapper.mapRow(rs, rs.getRow()));
            }
            return Lists.newArrayList(orders.values());
        }
    }

    private static class OrderMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("order_id"));
            order.setType(Order.Type.valueOf(rs.getString("type")));
            order.setUserId(rs.getLong("user_id"));
            order.setCreated(rs.getTimestamp("created").toLocalDateTime());
            order.setState(Order.State.valueOf(rs.getString("state")));
            order.setStateChanged(rs.getTimestamp("state_changed").toLocalDateTime());
            order.setStateComment(rs.getString("state_comment"));
            order.setRows(Lists.newArrayList());
            return order;
        }
    }

    private static class OrderDataMapper implements RowMapper<OrderData> {
        @Override
        public OrderData mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrderData data = new OrderData();
            data.setId(rs.getLong("order_data_id"));
            data.setOrderId(rs.getLong("order_id"));
            data.setProductId(rs.getLong("product_id"));
            data.setValue(rs.getInt("value"));
            return data;
        }
    }
}

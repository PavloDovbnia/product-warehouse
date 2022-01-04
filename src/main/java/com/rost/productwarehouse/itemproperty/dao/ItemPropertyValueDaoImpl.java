package com.rost.productwarehouse.itemproperty.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.itemproperty.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class ItemPropertyValueDaoImpl implements ItemPropertyValueDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ItemPropertyValueDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Map<Long, Map<String, ItemPropertyValue<?>>> getProductsProperties(List<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "select v.product_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from product_property_value v, item_property p " +
                    "where v.property_id = p.id " +
                    "    and v.product_id in (:productsIds) " +
                    "union " +
                    "select pg.product_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from product_to_product_group pg, product_group_property_value v, item_property p " +
                    "where pg.product_id in (:productsIds) " +
                    "    and pg.product_group_id = v.product_group_id " +
                    "    and v.property_id = p.id " +
                    "union " +
                    "select pg.product_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from product_to_product_group pg, product_group_to_manufacturer gm, manufacturer_property_value v, item_property p " +
                    "where pg.product_id in (:productsIds) " +
                    "    and pg.product_group_id = gm.product_group_id " +
                    "    and gm.manufacturer_id = v.manufacturer_id " +
                    "    and v.property_id = p.id";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("productsIds", productsIds), new PropertiesValuesExtractor());
        }
        return Maps.newTreeMap();
    }

    @Override
    public Map<Long, Map<String, ItemPropertyValue<?>>> getGroupsProperties(List<Long> groupsIds) {
        if (CollectionUtils.isNotEmpty(groupsIds)) {
            String sql = "select v.product_group_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from product_group_property_value v, item_property p " +
                    "where v.product_group_id in (:groupsIds) " +
                    "    and v.property_id = p.id " +
                    "union " +
                    "select gm.product_group_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from product_group_to_manufacturer gm, manufacturer_property_value v, item_property p " +
                    "where gm.product_group_id in (:groupsIds) " +
                    "    and gm.manufacturer_id = v.manufacturer_id " +
                    "    and v.property_id = p.id";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("groupsIds", groupsIds), new PropertiesValuesExtractor());
        }
        return Maps.newTreeMap();
    }

    @Override
    public Map<Long, Map<String, ItemPropertyValue<?>>> getManufacturersProperties(List<Long> manufacturersIds) {
        if (CollectionUtils.isNotEmpty(manufacturersIds)) {
            String sql = "select v.manufacturer_id as item_id, p.id, token, name, item_level, type, data_type, v.property_value " +
                    "from manufacturer_property_value v, item_property p " +
                    "where v.manufacturer_id in (:manufacturersIds) " +
                    "    and v.property_id = p.id";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("manufacturersIds", manufacturersIds), new PropertiesValuesExtractor());
        }
        return Maps.newTreeMap();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void saveProductValues(long productId, Map<String, String> values) {
        deleteProductValues(productId, values);
        addProductValues(productId, values);
    }

    private void deleteProductValues(long productId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "delete v from product_property_value v " +
                    "join item_property p " +
                    "on v.property_id = p.id " +
                    "where v.product_id = :productId " +
                    "and  p.token in (:tokens) " +
                    "and p.item_level = :itemLevel ";
            SqlParameterSource params = new MapSqlParameterSource("productId", productId)
                    .addValue("tokens", values.keySet())
                    .addValue("itemLevel", ItemLevel.PRODUCT.name());
            jdbcTemplate.update(sql, params);
        }
    }

    private void addProductValues(long productId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "insert into product_property_value (product_id, property_id, property_value) " +
                    "select :productId, p.id, :value " +
                    "from item_property p " +
                    "where p.token = :token " +
                    "and p.item_level = :itemLevel";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[values.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                batchParams[i] = new MapSqlParameterSource("productId", productId)
                        .addValue("value", entry.getValue())
                        .addValue("token", entry.getKey())
                        .addValue("itemLevel", ItemLevel.PRODUCT.name());
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void saveGroupValues(long groupId, Map<String, String> values) {
        deleteGroupValues(groupId, values);
        addGroupValues(groupId, values);
    }

    private void deleteGroupValues(long groupId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "delete v from product_group_property_value v " +
                    "join item_property p " +
                    "on v.property_id = p.id " +
                    "where v.product_group_id = :groupId " +
                    "and p.token in (:tokens) " +
                    "and p.item_level = :itemLevel";
            SqlParameterSource params = new MapSqlParameterSource("groupId", groupId)
                    .addValue("tokens", values.keySet())
                    .addValue("itemLevel", ItemLevel.PRODUCT_GROUP.name());
            jdbcTemplate.update(sql, params);
        }
    }

    private void addGroupValues(long groupId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "insert into product_group_property_value (product_group_id, property_id, property_value) " +
                    "select :groupId, p.id, :value " +
                    "from item_property p " +
                    "where p.token = :token " +
                    "and p.item_level = :itemLevel";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[values.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                batchParams[i] = new MapSqlParameterSource("groupId", groupId)
                        .addValue("value", entry.getValue())
                        .addValue("token", entry.getKey())
                        .addValue("itemLevel", ItemLevel.PRODUCT_GROUP.name());
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void saveManufacturerValues(long manufacturerId, Map<String, String> values) {
        deleteManufacturerValues(manufacturerId, values);
        addManufacturerValues(manufacturerId, values);
    }

    private void deleteManufacturerValues(long manufacturerId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "delete v from manufacturer_property_value v " +
                    "join item_property p " +
                    "on v.property_id = p.id " +
                    "where v.manufacturer_id = :manufacturerId " +
                    "and p.token in (:tokens) " +
                    "and p.item_level = :itemLevel";
            SqlParameterSource params = new MapSqlParameterSource("manufacturerId", manufacturerId)
                    .addValue("tokens", values.keySet())
                    .addValue("itemLevel", ItemLevel.MANUFACTURER.name());
            jdbcTemplate.update(sql, params);
        }
    }

    private void addManufacturerValues(long manufacturerId, Map<String, String> values) {
        if (MapUtils.isNotEmpty(values)) {
            String sql = "insert into manufacturer_property_value (manufacturer_id, property_id, property_value) " +
                    "select :manufacturerId, p.id, :value " +
                    "from item_property p " +
                    "where p.token = :token " +
                    "and p.item_level = :itemLevel";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[values.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : values.entrySet()) {
                batchParams[i] = new MapSqlParameterSource("manufacturerId", manufacturerId)
                        .addValue("value", entry.getValue())
                        .addValue("token", entry.getKey())
                        .addValue("itemLevel", ItemLevel.MANUFACTURER.name());
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void deleteProductValues(long productId) {
        String sql = "delete from product_property_value where product_id = :productId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("productId", productId));
    }

    @Override
    public void deleteGroupValues(long groupId) {
        String sql = "delete from product_group_property_value where product_group_id = :groupId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("groupId", groupId));
    }

    @Override
    public void deleteManufacturerValues(long manufacturerId) {
        String sql = "delete from manufacturer_property_value where manufacturer_id = :manufacturerId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("manufacturerId", manufacturerId));
    }

    private static class PropertiesValuesExtractor implements ResultSetExtractor<Map<Long, Map<String, ItemPropertyValue<?>>>> {

        private ItemPropertyDaoImpl.ItemPropertyRowMapper propertyRowMapper = new ItemPropertyDaoImpl.ItemPropertyRowMapper();

        @Override
        public Map<Long, Map<String, ItemPropertyValue<?>>> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Map<String, ItemPropertyValue<?>>> itemsValues = Maps.newHashMap();

            while (rs.next()) {
                Map<String, ItemPropertyValue<?>> itemValues = itemsValues.computeIfAbsent(rs.getLong("item_id"), key -> Maps.newTreeMap());
                ItemProperty property = propertyRowMapper.mapRow(rs, rs.getRow());
                if (!itemValues.containsKey(property.getToken())) {
                    switch (property.getType()) {
                        case SINGLE:
                            itemValues.put(property.getToken(), mapSingleValue(property, rs));
                            break;
                        case MULTIPLE:
                            itemValues.put(property.getToken(), mapMultipleValue(property, rs));
                            break;
                    }
                }
            }

            return itemsValues;
        }

        private ItemPropertyValue<?> mapSingleValue(ItemProperty property, ResultSet rs) throws SQLException, DataAccessException {
            Object value = null;
            switch (property.getDataType()) {
                case STRING:
                    value = rs.getString("property_value");
                    break;
                case BIG_DECIMAL:
                    value = rs.getBigDecimal("property_value");
                    break;
                case BOOLEAN:
                    value = rs.getBoolean("property_value");
                    break;
                case INTEGER:
                    value = rs.getInt("property_value");
                    break;
            }
            return value != null ? new SingleItemPropertyValue<>(value) : null;
        }

        private ItemPropertyValue<?> mapMultipleValue(ItemProperty property, ResultSet rs) throws SQLException, DataAccessException {
            String json = rs.getString("property_value");
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<?> typeReference = null;
            switch (property.getDataType()) {
                case STRING:
                    typeReference = new TypeReference<List<String>>() {
                    };
                    break;
                case BIG_DECIMAL:
                    typeReference = new TypeReference<List<BigDecimal>>() {
                    };
                    break;
                case BOOLEAN:
                    typeReference = new TypeReference<List<Boolean>>() {
                    };
                    break;
                case INTEGER:
                    typeReference = new TypeReference<List<Integer>>() {
                    };
                    break;
            }

            try {
                List<?> list = (List<?>) objectMapper.readValue(json, typeReference);
                return new MultipleItemPropertyValue<>(list);
            } catch (JsonProcessingException e) {
                throw new SQLException(e);
            }
        }
    }
}

package com.rost.productwarehouse.itemproperty.dao;

import com.google.common.collect.Maps;
import com.rost.productwarehouse.itemproperty.ItemLevel;
import com.rost.productwarehouse.itemproperty.ItemProperty;
import com.rost.productwarehouse.itemproperty.ItemPropertyValueDataType;
import com.rost.productwarehouse.itemproperty.ItemPropertyValueType;
import com.rost.productwarehouse.utils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemPropertyDaoImpl implements ItemPropertyDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ItemPropertyDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public ItemProperty getProperty(String token, ItemLevel itemLevel) {
        String sql = "select id, token, name, item_level, type, data_type " +
                "from item_property " +
                "where token = :token " +
                "and item_level = :itemLevel";
        SqlParameterSource params = new MapSqlParameterSource("token", token)
                .addValue("itemLevel", itemLevel.name());
        return DbUtils.extract(jdbcTemplate.query(sql, params, new ItemPropertyRowMapper()));
    }

    @Override
    public List<ItemProperty> getProperties(List<ItemLevel> itemLevels) {
        String sql = "select id, token, name, item_level, type, data_type " +
                "from item_property " +
                "where item_level in (:itemLevels) ";
        SqlParameterSource params = new MapSqlParameterSource("itemLevels", itemLevels.stream().map(ItemLevel::getName).collect(Collectors.toList()));
        return jdbcTemplate.query(sql, params, new ItemPropertiesExtractor());
    }

    @Override
    public void saveProperty(ItemProperty itemProperty) {
        String sql = "insert into item_property(token, name, item_level, type, data_type) " +
                "values (:token, :name, :itemLevel, :type, :dataType)";
        SqlParameterSource params = new MapSqlParameterSource("token", itemProperty.getToken())
                .addValue("name", itemProperty.getName())
                .addValue("itemLevel", itemProperty.getItemLevel().name())
                .addValue("type", itemProperty.getType().name())
                .addValue("dataType", itemProperty.getDataType().name());
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteProperty(long id) {
        String sql = "delete from item_property where id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    private static class ItemPropertiesExtractor implements ResultSetExtractor<List<ItemProperty>> {

        private ItemPropertyRowMapper mapper = new ItemPropertyRowMapper();

        @Override
        public List<ItemProperty> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<String, Map<ItemLevel, ItemProperty>> properties = Maps.newHashMap();
            while (rs.next()) {
                ItemProperty property = mapper.mapRow(rs, rs.getRow());
                Map<ItemLevel, ItemProperty> tokenProperties = properties.computeIfAbsent(property.getToken(), key -> Maps.newHashMap());
                switch (property.getItemLevel()) {
                    case PRODUCT:
                        tokenProperties.put(property.getItemLevel(), property);
                        break;
                    case PRODUCT_GROUP:
                        if (!tokenProperties.containsKey(ItemLevel.PRODUCT)) {
                            tokenProperties.put(property.getItemLevel(), property);
                        }
                        break;
                    case MANUFACTURER:
                        if (tokenProperties.isEmpty()) {
                            tokenProperties.put(property.getItemLevel(), property);
                        }
                        break;
                }
            }
            return properties.entrySet().stream().flatMap(entry -> entry.getValue().entrySet().stream()).map(Map.Entry::getValue).collect(Collectors.toList());
        }
    }

    public static class ItemPropertyRowMapper implements RowMapper<ItemProperty> {
        @Override
        public ItemProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
            ItemProperty property = new ItemProperty();

            property.setId(rs.getLong("id"));
            property.setToken(rs.getString("token"));
            property.setName(rs.getString("name"));
            property.setItemLevel(ItemLevel.of(rs.getString("item_level")));
            property.setType(ItemPropertyValueType.of(rs.getString("type")));
            property.setDataType(ItemPropertyValueDataType.of(rs.getString("data_type")));

            return property;
        }
    }
}

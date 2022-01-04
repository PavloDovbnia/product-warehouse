package com.rost.productwarehouse.productstockdata.dao;

import com.google.common.collect.Maps;
import com.rost.productwarehouse.productstockdata.ProductStockData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

@Repository
public class ProductStockDataDaoImpl implements ProductStockDataDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductStockDataDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Map<Long, ProductStockData> getProductsStockData(Collection<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "select product_id, stock_value " +
                    "from product_stock_data " +
                    "where product_id in (:productsIds)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("productsIds", productsIds), new ProductsStockDataExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public Map<Long, ProductStockData> getProductsStockDataLessValue(int value) {
        String sql = "select product_id, stock_value " +
                "from product_stock_data " +
                "where stock_value < :value";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("value", value), new ProductsStockDataExtractor());
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void saveProductsStockData(Collection<ProductStockData> productsStockData) {
        if (CollectionUtils.isNotEmpty(productsStockData)) {
            String sql = "insert into product_stock_data (product_id, stock_value) " +
                    "values (:productId, :stockValue)";

            jdbcTemplate.batchUpdate(sql, getBatchParams(productsStockData));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void updateProductsStockData(Collection<ProductStockData> productsStockData) {
        if (CollectionUtils.isNotEmpty(productsStockData)) {
            String sql = "update product_stock_data " +
                    "set stock_value = stock_value + :stockValue " +
                    "where product_id = :productId";

            jdbcTemplate.batchUpdate(sql, getBatchParams(productsStockData));
        }
    }

    private SqlParameterSource[] getBatchParams(Collection<ProductStockData> productsStockData) {
        SqlParameterSource[] batchParams = new MapSqlParameterSource[productsStockData.size()];
        int i = 0;
        for (ProductStockData data : productsStockData) {
            batchParams[i] = new MapSqlParameterSource("productId", data.getProductId())
                    .addValue("stockValue", data.getStockValue());
            i++;
        }
        return batchParams;
    }

    private static class ProductsStockDataExtractor implements ResultSetExtractor<Map<Long, ProductStockData>> {

        private ProductStockDataMapper stockDataMapper = new ProductStockDataMapper();

        @Override
        public Map<Long, ProductStockData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, ProductStockData> productsStockData = Maps.newHashMap();
            while (rs.next()) {
                ProductStockData data = stockDataMapper.mapRow(rs, rs.getRow());
                productsStockData.put(data.getProductId(), data);
            }
            return productsStockData;
        }
    }

    private static class ProductStockDataMapper implements RowMapper<ProductStockData> {
        @Override
        public ProductStockData mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductStockData data = new ProductStockData();
            data.setProductId(rs.getLong("product_id"));
            data.setStockValue(rs.getInt("stock_value"));
            return data;
        }
    }
}

package com.rost.productwarehouse.product.dao;

import com.google.common.collect.Maps;
import com.rost.productwarehouse.product.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class ProductDaoImpl implements ProductDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Product> getProducts() {
        String sql = "select id, name from product";
        return jdbcTemplate.query(sql, new ProductMapper());
    }

    @Override
    public Map<Long, Product> getProducts(Collection<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "select id, name from product where id in (:productsIds)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("productsIds", productsIds), new ProductsExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public long storeProduct(Product product) {
        return product.isNew() ? addProduct(product) : editProduct(product);
    }

    private long addProduct(Product product) {
        String sql = "insert into product(name) values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource("name", product.getName()), keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long editProduct(Product product) {
        String sql = "update product set name = :name where id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", product.getId())
                .addValue("name", product.getName());
        jdbcTemplate.update(sql, params);
        return product.getId();
    }

    @Override
    public void deleteProduct(long productId) {
        String sql = "delete from product " +
                "where id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", productId));
    }

    @Override
    public void deleteProductFromGroup(long productId) {
        String sql = "delete from product_to_product_group where product_id = :productId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("productId", productId));
    }

    private static class ProductsExtractor implements ResultSetExtractor<Map<Long, Product>> {

        private ProductMapper productMapper = new ProductMapper();

        @Override
        public Map<Long, Product> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Product> products = Maps.newTreeMap();
            while (rs.next()) {
                Product product = productMapper.mapRow(rs, rs.getRow());
                products.put(product.getId(), product);
            }
            return products;
        }
    }

    private static class ProductMapper implements RowMapper<Product> {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Product(rs.getLong("id"), rs.getString("name"));
        }
    }
}

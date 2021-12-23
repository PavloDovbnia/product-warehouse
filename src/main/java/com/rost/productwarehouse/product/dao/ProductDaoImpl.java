package com.rost.productwarehouse.product.dao;

import com.google.common.collect.Lists;
import com.rost.productwarehouse.product.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Product(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public List<Product> getProducts(List<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "select id, name from product where id in (:productsIds)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("productsIds", productsIds), (rs, rowNum) -> new Product(rs.getLong("id"), rs.getString("name")));
        }
        return Lists.newArrayList();
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
}

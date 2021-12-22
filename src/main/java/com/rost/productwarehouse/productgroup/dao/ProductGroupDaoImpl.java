package com.rost.productwarehouse.productgroup.dao;

import com.rost.productwarehouse.productgroup.ProductGroup;
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
public class ProductGroupDaoImpl implements ProductGroupDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ProductGroupDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<ProductGroup> getGroups(List<Long> groupsIds) {
        String sql = "select id, name from product_group where id in (:ids)";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("ids", groupsIds), (rs, rowNum) -> new ProductGroup(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public long storeProductGroup(ProductGroup productGroup) {
        return productGroup.isNew() ? addProductGroup(productGroup) : editProductGroup(productGroup);
    }

    private long addProductGroup(ProductGroup productGroup) {
        String sql = "insert into product_group (name) values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource("name", productGroup.getName()), keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long editProductGroup(ProductGroup productGroup) {
        String sql = "update product_group set name = :name where id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", productGroup.getId())
                .addValue("name", productGroup.getName());
        jdbcTemplate.update(sql, params);
        return productGroup.getId();
    }

    @Override
    public void deleteProductGroup(long productGroupId) {
        String sql = "delete from product_group where id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", productGroupId));
    }
}

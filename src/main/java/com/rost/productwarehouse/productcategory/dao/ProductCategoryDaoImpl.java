package com.rost.productwarehouse.productcategory.dao;

import com.rost.productwarehouse.productcategory.ProductCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductCategoryDaoImpl implements ProductCategoryDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductCategoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<ProductCategory> getCategories() {
        String sql = "select id, name from product_group_category";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ProductCategory category = new ProductCategory();
            category.setId(rs.getLong("id"));
            category.setName(rs.getString("name"));
            return category;
        });
    }

    @Override
    public long saveCategory(ProductCategory productCategory) {
        return productCategory.isNew() ? addCategory(productCategory) : editCategory(productCategory);
    }

    private long addCategory(ProductCategory category) {
        String sql = "insert into product_group_category (name) values (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource("name", category.getName()), keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long editCategory(ProductCategory category) {
        String sql = "update product_group_category set name = :name where id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", category.getId())
                .addValue("name", category.getName());
        jdbcTemplate.update(sql, params);
        return category.getId();
    }

    @Override
    public void removeCategory(long categoryId) {
        String sql = "delete from product_group_category where id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", categoryId));
    }

    @Override
    public void removeCategoryGroups(long categoryId) {
        String sql = "delete from product_group_to_product_group_category where product_group_category_id = :categoryId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("categoryId", categoryId));
    }
}

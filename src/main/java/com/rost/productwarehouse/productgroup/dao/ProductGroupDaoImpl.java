package com.rost.productwarehouse.productgroup.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rost.productwarehouse.productcategory.ProductCategory;
import com.rost.productwarehouse.productgroup.ProductGroup;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public class ProductGroupDaoImpl implements ProductGroupDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ProductGroupDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<ProductGroup> getGroups() {
        String sql = "select g.id product_group_id, g.name product_group_name, pg.product_id, gc.product_group_category_id category_id from product_group g " +
                "left join product_to_product_group pg " +
                "on g.id = pg.product_group_id " +
                "left join product_group_to_product_group_category gc " +
                "on g.id = gc.product_group_id ";
        return jdbcTemplate.query(sql, new ProductGroupExtractor());
    }

    @Override
    public List<ProductGroup> getGroups(List<Long> groupsIds) {
        if (CollectionUtils.isNotEmpty(groupsIds)) {
            String sql = "select g.id product_group_id, g.name product_group_name, pg.product_id, gc.product_group_category_id category_id from product_group g " +
                    "left join product_to_product_group pg " +
                    "on g.id = pg.product_group_id " +
                    "left join product_group_to_product_group_category gc " +
                    "on g.id = gc.product_group_id " +
                    "where g.id in (:ids)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("ids", groupsIds), new ProductGroupExtractor());
        }
        return Lists.newArrayList();
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
        String sql = "delete pg, g from product_group g " +
                "left join product_to_product_group pg " +
                "on g.id = pg.product_group_id " +
                "where g.id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", productGroupId));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void storeGroupProducts(long groupId, Collection<Long> productsIds) {
        deleteProductsFromGroup(groupId);
        addProductsToGroup(groupId, productsIds);
    }

    private void addProductsToGroup(long groupId, Collection<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "insert into product_to_product_group (product_id, product_group_id) " +
                    "values (:productId, :groupId)";
            SqlParameterSource[] batchParams = new MapSqlParameterSource[productsIds.size()];
            int i = 0;
            for (Long productId : productsIds) {
                batchParams[i] = new MapSqlParameterSource("productId", productId)
                        .addValue("groupId", groupId);
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void deleteProductsFromGroup(long groupId) {
        String sql = "delete from product_to_product_group " +
                "where product_group_id = :groupId ";
        jdbcTemplate.update(sql, new MapSqlParameterSource("groupId", groupId));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void storeGroupCategory(long groupId, long categoryId) {
        deleteGroupCategory(groupId);
        addGroupCategory(groupId, categoryId);
    }

    private void addGroupCategory(long groupId, long categoryId) {
        String sql = "insert into product_group_to_product_group_category (product_group_id, product_group_category_id) " +
                "values (:groupId, :categoryId)";
        SqlParameterSource params = new MapSqlParameterSource("groupId", groupId)
                .addValue("categoryId", categoryId);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteGroupCategory(long groupId) {
        String sql = "delete from product_group_to_product_group_category where product_group_id = :groupId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("groupId", groupId));
    }

    @Override
    public void deleteGroupFromManufacturer(long groupId) {
        String sql = "delete from product_group_to_manufacturer where product_group_id = :groupId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("groupId", groupId));
    }

    private static class ProductGroupExtractor implements ResultSetExtractor<List<ProductGroup>> {

        private ProductGroupMapper groupMapper = new ProductGroupMapper();

        @Override
        public List<ProductGroup> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, ProductGroup> groups = Maps.newTreeMap();
            while (rs.next()) {
                ProductGroup mappedGroup = groupMapper.mapRow(rs, rs.getRow());
                groups.putIfAbsent(mappedGroup.getId(), mappedGroup);

                ProductGroup group = groups.get(mappedGroup.getId());
                long productId = rs.getLong("product_id");
                if (!rs.wasNull()) {
                    group.getProductsIds().add(productId);
                }
            }
            return Lists.newArrayList(groups.values());
        }
    }

    private static class ProductGroupMapper implements RowMapper<ProductGroup> {
        @Override
        public ProductGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProductGroup group = new ProductGroup();
            group.setId(rs.getLong("product_group_id"));
            group.setName(rs.getString("product_group_name"));

            long categoryId = rs.getLong("category_id");
            if (!rs.wasNull()) {
                ProductCategory category = new ProductCategory();
                category.setId(categoryId);
                group.setCategory(category);
            }

            group.setProductsIds(Sets.newTreeSet());
            return group;
        }
    }
}

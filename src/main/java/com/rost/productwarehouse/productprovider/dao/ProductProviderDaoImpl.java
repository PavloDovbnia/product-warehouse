package com.rost.productwarehouse.productprovider.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rost.productwarehouse.productprovider.ProductProvider;
import com.rost.productwarehouse.security.Role;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ProductProviderDaoImpl implements ProductProviderDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ProductProviderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Map<Long, ProductProvider> getProviders() {
        String sql = "select u.id user_id, p.product_id " +
                "from users u " +
                "    join user_roles ur " +
                "        on u.id = ur.user_id " +
                "    join roles r " +
                "        on ur.role_id = r.id " +
                "    left join product_to_provider p " +
                "        on u.id = p.user_id " +
                "where r.type = :roleType ";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("roleType", Role.Type.ROLE_PRODUCT_PROVIDER.name()), new ProvidersExtractor());
    }

    @Override
    public Map<Long, List<ProductProvider>> getProductProviders(Collection<Long> productsIds) {
        if (CollectionUtils.isNotEmpty(productsIds)) {
            String sql = "select user_id, product_id " +
                    "from product_to_provider " +
                    "where product_id in (:productsIds)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("productsIds", productsIds), new ProductsProvidersExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public Map<Long, ProductProvider> getProviders(Collection<Long> providersIds) {
        if (CollectionUtils.isNotEmpty(providersIds)) {
            String sql = "select u.id user_id, p.product_id " +
                    "from users u " +
                    "    join user_roles ur " +
                    "        on u.id = ur.user_id " +
                    "    join roles r " +
                    "        on ur.role_id = r.id " +
                    "    left join product_to_provider p " +
                    "        on u.id = p.user_id " +
                    "where r.type = :roleType " +
                    "and u.id in (:providersIds)";
            SqlParameterSource params = new MapSqlParameterSource("providersIds", providersIds)
                    .addValue("roleType", Role.Type.ROLE_PRODUCT_PROVIDER.name());
            return jdbcTemplate.query(sql, params, new ProvidersExtractor());
        }
        return Maps.newHashMap();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void saveProviders(Collection<ProductProvider> providers) {
        deleteProviders(providers.stream().map(ProductProvider::getProviderId).collect(Collectors.toList()));
        addProviders(providers);
    }

    private void addProviders(Collection<ProductProvider> providers) {
        providers = providers.stream().filter(provider -> CollectionUtils.isNotEmpty(provider.getProductsIds())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(providers)) {
            String sql = "insert into product_to_provider (user_id, product_id)" +
                    "values (:providerId, :productId) ";

            int totalInserts = providers.stream().map(provider -> provider.getProductsIds().size()).reduce(0, Integer::sum);

            SqlParameterSource[] batchParams = new MapSqlParameterSource[totalInserts];
            int i = 0;
            for (ProductProvider provider : providers) {
                for (long productId : provider.getProductsIds()) {
                    batchParams[i] = new MapSqlParameterSource("providerId", provider.getProviderId())
                            .addValue("productId", productId);
                    i++;
                }
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void deleteProviders(Collection<Long> providersIds) {
        if (CollectionUtils.isNotEmpty(providersIds)) {
            String sql = "delete from product_to_provider where user_id in (:providersIds)";
            jdbcTemplate.update(sql, new MapSqlParameterSource("providersIds", providersIds));
        }
    }

    private static class ProvidersExtractor implements ResultSetExtractor<Map<Long, ProductProvider>> {
        @Override
        public Map<Long, ProductProvider> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, ProductProvider> providers = Maps.newTreeMap();
            while (rs.next()) {
                long providerId = rs.getLong("user_id");
                ProductProvider provider = providers.get(providerId);
                if (provider == null) {
                    provider = new ProductProvider();
                    provider.setProviderId(providerId);
                    provider.setProductsIds(Sets.newTreeSet());
                    providers.put(providerId, provider);
                }
                long productId = rs.getLong("product_id");
                if (!rs.wasNull()) {
                    provider.getProductsIds().add(productId);
                }
            }
            return providers;
        }
    }

    private static class ProductsProvidersExtractor implements ResultSetExtractor<Map<Long, List<ProductProvider>>> {
        @Override
        public Map<Long, List<ProductProvider>> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, List<ProductProvider>> productsProviders = Maps.newHashMap();
            while (rs.next()) {
                long providerId = rs.getLong("user_id");
                long productId = rs.getLong("product_id");
                ProductProvider provider = new ProductProvider();
                provider.setProviderId(providerId);
                provider.setProductsIds(Sets.newHashSet(productId));
                productsProviders.computeIfAbsent(productId, key -> Lists.newArrayList()).add(provider);
            }
            return productsProviders;
        }
    }
}

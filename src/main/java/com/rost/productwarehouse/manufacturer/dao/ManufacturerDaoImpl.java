package com.rost.productwarehouse.manufacturer.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rost.productwarehouse.manufacturer.Manufacturer;
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
public class ManufacturerDaoImpl implements ManufacturerDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ManufacturerDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Manufacturer> getManufacturers() {
        String sql = "select m.id manufacturer_id, m.name manufacturer_name, gm.product_group_id from manufacturer m " +
                "left join product_group_to_manufacturer gm " +
                "on m.id = gm.manufacturer_id";
        Map<Long, Manufacturer> manufacturers = jdbcTemplate.query(sql, new ManufacturersExtractor());
        return Lists.newArrayList(manufacturers.values());
    }

    @Override
    public Map<Long, Manufacturer> getManufacturers(Collection<Long> manufacturerIds) {
        if (CollectionUtils.isNotEmpty(manufacturerIds)) {
            String sql = "select m.id manufacturer_id, m.name manufacturer_name, gm.product_group_id from manufacturer m " +
                    "left join product_group_to_manufacturer gm " +
                    "on m.id = gm.manufacturer_id";
            return jdbcTemplate.query(sql, new ManufacturersExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public long storeManufacturer(Manufacturer manufacturer) {
        return manufacturer.isNew() ? addManufacturer(manufacturer) : editManufacturer(manufacturer);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void storeManufacturerGroups(long manufacturerId, Collection<Long> groupsIds) {
        deleteManufacturerGroups(manufacturerId);
        addManufacturerGroups(manufacturerId, groupsIds);
    }

    private void addManufacturerGroups(long manufacturerId, Collection<Long> groupsIds) {
        if (CollectionUtils.isNotEmpty(groupsIds)) {
            String sql = "insert into product_group_to_manufacturer (product_group_id, manufacturer_id) " +
                    "values (:groupId, :manufacturerId)";

            SqlParameterSource[] batchParams = new MapSqlParameterSource[groupsIds.size()];
            int i = 0;
            for (long groupId : groupsIds) {
                batchParams[i] = new MapSqlParameterSource("manufacturerId", manufacturerId)
                        .addValue("groupId", groupId);
                i++;
            }
            jdbcTemplate.batchUpdate(sql, batchParams);
        }
    }

    @Override
    public void deleteManufacturerGroups(long manufacturerId) {
        String sql = "delete from product_group_to_manufacturer where manufacturer_id = :manufacturerId ";
        jdbcTemplate.update(sql, new MapSqlParameterSource("manufacturerId", manufacturerId));
    }

    private long addManufacturer(Manufacturer manufacturer) {
        String sql = "insert into manufacturer (name) value (:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, new MapSqlParameterSource("name", manufacturer.getName()), keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long editManufacturer(Manufacturer manufacturer) {
        String sql = "update manufacturer set name = :name where id = :id";
        SqlParameterSource params = new MapSqlParameterSource("id", manufacturer.getId())
                .addValue("name", manufacturer.getName());
        jdbcTemplate.update(sql, params);
        return manufacturer.getId();
    }

    @Override
    public void deleteManufacturer(long manufacturerId) {
        String sql = "delete from manufacturer where id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", manufacturerId));
    }

    private static class ManufacturersExtractor implements ResultSetExtractor<Map<Long, Manufacturer>> {
        private ManufacturerMapper manufacturerMapper = new ManufacturerMapper();

        @Override
        public Map<Long, Manufacturer> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Manufacturer> manufacturers = Maps.newTreeMap();
            while (rs.next()) {
                Manufacturer mappedManufacturer = manufacturerMapper.mapRow(rs, rs.getRow());
                manufacturers.putIfAbsent(mappedManufacturer.getId(), mappedManufacturer);

                Manufacturer manufacturer = manufacturers.get(mappedManufacturer.getId());
                long groupId = rs.getLong("product_group_id");
                if (!rs.wasNull()) {
                    manufacturer.getProductGroupsIds().add(groupId);
                }
            }
            return manufacturers;
        }
    }

    private static class ManufacturerMapper implements RowMapper<Manufacturer> {
        @Override
        public Manufacturer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Manufacturer manufacturer = new Manufacturer();
            manufacturer.setId(rs.getLong("manufacturer_id"));
            manufacturer.setName(rs.getString("manufacturer_name"));
            manufacturer.setProductGroupsIds(Sets.newTreeSet());
            return manufacturer;
        }
    }
}

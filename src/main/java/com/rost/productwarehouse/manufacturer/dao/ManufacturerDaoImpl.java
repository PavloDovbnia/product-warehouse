package com.rost.productwarehouse.manufacturer.dao;

import com.rost.productwarehouse.manufacturer.Manufacturer;
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
public class ManufacturerDaoImpl implements ManufacturerDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ManufacturerDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Manufacturer> getManufacturers() {
        String sql = "select id, name from manufacturer";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Manufacturer(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public long storeManufacturer(Manufacturer manufacturer) {
        return manufacturer.isNew() ? addManufacturer(manufacturer) : editManufacturer(manufacturer);
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
}

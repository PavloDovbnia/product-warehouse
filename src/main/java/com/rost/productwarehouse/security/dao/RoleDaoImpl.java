package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.utils.DbUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleDaoImpl implements RoleDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public RoleDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Optional<Role> getRole(Role.Type roleType) {
        String sql = "select id, type from roles where type = :type";
        Role role = DbUtils.extract(jdbcTemplate.query(sql, new MapSqlParameterSource("type", roleType.name()), new RoleMapper()));
        return Optional.ofNullable(role);
    }
}

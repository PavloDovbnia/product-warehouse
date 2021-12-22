package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.Role;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleMapper implements RowMapper<Role> {

    private String prefix = "";

    public RoleMapper() {
    }

    public RoleMapper(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
        Role role = new Role();
        role.setId(rs.getLong(prefix + "id"));
        role.setType(Role.Type.valueOf(rs.getString(prefix + "type")));
        return role;
    }
}

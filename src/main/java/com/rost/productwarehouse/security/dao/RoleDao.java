package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.Role;

import java.util.Optional;

public interface RoleDao {

    Optional<Role> getRole(Role.Type roleType);
}

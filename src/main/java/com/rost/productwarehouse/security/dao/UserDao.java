package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.User;

import java.util.Optional;

public interface UserDao {

    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

    void save(User user);

    void saveUserPassword(long userId, String password);
}

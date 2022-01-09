package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserDao {

    List<User> getUsersByRoles(Collection<Role.Type> roleTypes);

    Map<Long, User> getUsers(Collection<Long> usersIds);

    List<User> getUsers();

    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

    Map<String, User> getByEmails(Collection<String> emails);

    void save(User user);

    void saveUserPassword(long userId, String password);

    void deleteUser(long userId);
}

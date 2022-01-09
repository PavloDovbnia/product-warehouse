package com.rost.productwarehouse.security.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.utils.DbUtils;
import org.apache.commons.collections4.CollectionUtils;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserDaoImpl implements UserDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<User> getUsersByRoles(Collection<Role.Type> roleTypes) {
        if (CollectionUtils.isNotEmpty(roleTypes)) {
            String sql = "select u.id user_id, username, email, null as password, r.id role_id, type role_type " +
                    "from users u " +
                    "left join user_roles ur on u.id = ur.user_id " +
                    "left join roles r on ur.role_id = r.id " +
                    "where r.type in (:roleTypes)";
            Map<Long, User> users = jdbcTemplate.query(sql, new MapSqlParameterSource("roleTypes", roleTypes.stream().map(Role.Type::name).collect(Collectors.toList())), new UsersExtractor());
            return Lists.newArrayList(users.values());
        }
        return Lists.newArrayList();
    }

    @Override
    public Map<Long, User> getUsers(Collection<Long> usersIds) {
        if (CollectionUtils.isNotEmpty(usersIds)) {
            String sql = "select u.id user_id, username, email, null as password, r.id role_id, type role_type " +
                    "from users u " +
                    "left join user_roles ur on u.id = ur.user_id " +
                    "left join roles r on ur.role_id = r.id " +
                    "where u.id in (:usersIds)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("usersIds", usersIds), new UsersExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public List<User> getUsers() {
        String sql = "select u.id user_id, username, email, null as password, r.id role_id, type role_type " +
                "from users u " +
                "left join user_roles ur on u.id = ur.user_id " +
                "left join roles r on ur.role_id = r.id ";
        return Lists.newArrayList(jdbcTemplate.query(sql, new UsersExtractor()).values());
    }

    @Override
    public Optional<User> getByUsername(String username) {
        String sql = "select u.id user_id, username, email, password, r.id role_id, type role_type " +
                "from users u " +
                "left join user_roles ur on u.id = ur.user_id " +
                "left join roles r on ur.role_id = r.id " +
                "where u.username = :username";
        Map<Long, User> users = jdbcTemplate.query(sql, new MapSqlParameterSource("username", username), new UsersExtractor());
        return DbUtils.extractOptional(users);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return DbUtils.extractOptional(getByEmails(Lists.newArrayList(email)));
    }

    @Override
    public Map<String, User> getByEmails(Collection<String> emails) {
        if (CollectionUtils.isNotEmpty(emails)) {
            String sql = "select u.id user_id, username, email, null as password, r.id role_id, type role_type " +
                    "from users u " +
                    "left join user_roles ur on u.id = ur.user_id " +
                    "left join roles r on ur.role_id = r.id " +
                    "where u.email in (:emails)";
            return jdbcTemplate.query(sql, new MapSqlParameterSource("emails", emails), new UsersMappedToEmailsExtractor());
        }
        return Maps.newHashMap();
    }

    @Override
    public void saveUserPassword(long userId, String password) {
        String sql = "update users set password = :password where id = :userId";
        SqlParameterSource params = new MapSqlParameterSource("userId", userId)
                .addValue("password", password);
        jdbcTemplate.update(sql, params);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void save(User user) {
        if (user.isNew()) {
            addUser(user);
        } else {
            editUser(user);
        }
        ;
        deleteUserRoles(user);
        addUserRoles(user);
    }

    private void addUser(User user) {
        String sql = "insert into users (username, email, password) " +
                "values (:username, :email, :password)";
        SqlParameterSource params = new MapSqlParameterSource("username", user.getUsername())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder);
        long userId = keyHolder.getKey().longValue();
        user.setId(userId);
    }

    private void editUser(User user) {
        String sql = "update users set email = :email where username = :username";
        SqlParameterSource params = new MapSqlParameterSource("username", user.getUsername())
                .addValue("email", user.getEmail());
        jdbcTemplate.update(sql, params);
    }

    private void addUserRoles(User user) {
        String sql = "insert into user_roles (user_id, role_id) " +
                "select :userId, id " +
                "from roles " +
                "where type in (:roles)";
        SqlParameterSource params = new MapSqlParameterSource("userId", user.getId())
                .addValue("roles", user.getRoles().stream().map(r -> r.getType().name()).collect(Collectors.toList()));
        jdbcTemplate.update(sql, params);
    }

    private void deleteUserRoles(User user) {
        String sql = "delete from user_roles where user_id = :userId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("userId", user.getId()));
    }

    @Override
    public void deleteUser(long userId) {
        String sql = "delete from users where id = :userId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("userId", userId));
    }

    private static class UsersMappedToEmailsExtractor implements ResultSetExtractor<Map<String, User>> {

        private UserMapper userMapper = new UserMapper();
        private RoleMapper roleMapper = new RoleMapper("role_");

        @Override
        public Map<String, User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<String, User> users = Maps.newTreeMap();
            while (rs.next()) {
                User user = userMapper.mapRow(rs, rs.getRow());
                users.putIfAbsent(user.getEmail(), user);
                user = users.get(user.getEmail());

                Role role = roleMapper.mapRow(rs, rs.getRow());
                user.getRoles().add(role);
            }
            return users;
        }
    }

    private static class UsersExtractor implements ResultSetExtractor<Map<Long, User>> {

        private UserMapper userMapper = new UserMapper();
        private RoleMapper roleMapper = new RoleMapper("role_");

        @Override
        public Map<Long, User> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, User> users = Maps.newTreeMap();
            while (rs.next()) {
                User user = userMapper.mapRow(rs, rs.getRow());
                users.putIfAbsent(user.getId(), user);
                user = users.get(user.getId());

                Role role = roleMapper.mapRow(rs, rs.getRow());
                user.getRoles().add(role);
            }
            return users;
        }
    }

    private static class UserMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            return user;
        }
    }
}

package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserDaoImpl implements UserDao {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Optional<User> getByUsername(String username) {
        String sql = "select u.id user_id, username, email, password, r.id role_id, type role_type " +
                "from users u " +
                "left join user_roles ur on u.id = ur.user_id " +
                "left join roles r on ur.role_id = r.id " +
                "where u.username = :username";
        User user = jdbcTemplate.query(sql, new MapSqlParameterSource("username", username), new UserExtractor());
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        String sql = "select u.id user_id, username, email, password, r.id role_id, type role_type " +
                "from users u " +
                "left join user_roles ur on u.id = ur.user_id " +
                "left join roles r on ur.role_id = r.id " +
                "where u.username = :email";
        User user = jdbcTemplate.query(sql, new MapSqlParameterSource("email", email), new UserExtractor());
        return Optional.ofNullable(user);
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
        String sql = "update users set password = :password where username = :username";
        SqlParameterSource params = new MapSqlParameterSource("username", user.getUsername())
                .addValue("password", user.getPassword());
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

    private static class UserExtractor implements ResultSetExtractor<User> {
        private UserMapper userMapper = new UserMapper();
        private RoleMapper roleMapper = new RoleMapper("role_");

        @Override
        public User extractData(ResultSet rs) throws SQLException, DataAccessException {
            User user = null;
            while (rs.next()) {
                if (user == null) {
                    user = userMapper.mapRow(rs, rs.getRow());
                }
                Role role = roleMapper.mapRow(rs, rs.getRow());
                user.getRoles().add(role);
            }
            return user;
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

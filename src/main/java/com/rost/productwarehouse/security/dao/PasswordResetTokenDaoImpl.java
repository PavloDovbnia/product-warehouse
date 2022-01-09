package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.PasswordResetToken;
import com.rost.productwarehouse.utils.DbUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PasswordResetTokenDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void saveToken(PasswordResetToken token) {
        String sql = "insert into password_reset_token (token, user_id) values (:token, :userId) " +
                "on duplicate key update token = :token, created = :now ";
        SqlParameterSource params = new MapSqlParameterSource("userId", token.getUserId())
                .addValue("token", token.getToken())
                .addValue("now", Timestamp.valueOf(LocalDateTime.now()));
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteToken(long userId) {
        String sql = "delete from password_reset_token where user_id = :userId";
        jdbcTemplate.update(sql, new MapSqlParameterSource("userId", userId));
    }

    @Override
    public void deleteTokens(LocalDateTime fromDate) {
        String sql = "delete from password_reset_token where created < :fromDate";
        jdbcTemplate.update(sql, new MapSqlParameterSource("fromDate", Timestamp.valueOf(fromDate)));
    }

    @Override
    public Optional<PasswordResetToken> getToken(String token) {
        String sql = "select token, user_id, created from password_reset_token where token = :token";
        PasswordResetToken passwordResetToken = DbUtils.extract(jdbcTemplate.query(sql, new MapSqlParameterSource("token", token), (rs, rowNum) ->
                new PasswordResetToken(rs.getString("token"), rs.getLong("user_id"), rs.getTimestamp("created").toLocalDateTime())));
        return Optional.ofNullable(passwordResetToken);
    }
}

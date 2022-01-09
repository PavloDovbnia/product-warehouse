package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenDao {

    void saveToken(PasswordResetToken token);

    void deleteToken(long userId);

    void deleteTokens(LocalDateTime fromDate);

    Optional<PasswordResetToken> getToken(String token);
}

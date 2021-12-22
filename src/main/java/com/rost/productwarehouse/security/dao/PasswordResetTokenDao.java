package com.rost.productwarehouse.security.dao;

import com.rost.productwarehouse.security.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenDao {

    void saveToken(PasswordResetToken token);

    Optional<PasswordResetToken> getToken(String token);
}

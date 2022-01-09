package com.rost.productwarehouse.security.service;

import com.rost.productwarehouse.security.PasswordResetToken;
import com.rost.productwarehouse.security.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getUsers();

    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

    void save(User user);

    void deleteUser(long userId);

    void changePassword(String currentPassword, String newPassword);

    void changePasswordByToken(String token, String newPassword);

    PasswordResetToken createPasswordResetToken(String email);

    PasswordResetToken validatePasswordResetToken(String token);

    void deleteExpiredPasswordResetTokens();
}

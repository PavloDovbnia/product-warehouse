package com.rost.productwarehouse.security.service;

import com.rost.productwarehouse.security.PasswordResetToken;

public interface UserService {

    void changePasswordByToken(String token, String newPassword);

    PasswordResetToken createPasswordResetToken(String email);

    PasswordResetToken validatePasswordResetToken(String token);
}

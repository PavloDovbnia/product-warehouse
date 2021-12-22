package com.rost.productwarehouse.security.service;

import com.rost.productwarehouse.security.PasswordResetToken;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.PasswordResetTokenDao;
import com.rost.productwarehouse.security.dao.UserDao;
import com.rost.productwarehouse.security.exceptions.PasswordResetTokenValidationException;
import com.rost.productwarehouse.security.exceptions.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserDao userDao, PasswordResetTokenDao passwordResetTokenDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.passwordResetTokenExpirationMs}")
    private long passwordResetTokenExpirationMs;
    @Value("${app.minPasswordLength}")
    private int minPasswordLength;

    @Override
    public PasswordResetToken createPasswordResetToken(String email) {
        User user = userDao.getByEmail(email).orElseThrow(() -> new UserNotFoundException("User was not found by " + email));
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user.getId());
        passwordResetTokenDao.saveToken(passwordResetToken);
        return passwordResetToken;
    }

    @Override
    public PasswordResetToken validatePasswordResetToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new PasswordResetTokenValidationException("Input token is not valid");
        }
        PasswordResetToken passwordResetToken = passwordResetTokenDao.getToken(token).orElseThrow(() -> new PasswordResetTokenValidationException("Password Reset Token was not found by " + token));
        if (LocalDateTime.now().isAfter(passwordResetToken.getCreated().plusNanos(passwordResetTokenExpirationMs * 1000L))) {
            throw new PasswordResetTokenValidationException("Password Reset Token has been expired, please create new one");
        }
        return passwordResetToken;
    }

    @Override
    public void changePasswordByToken(String token, String newPassword) {
        if (StringUtils.isEmpty(newPassword) || newPassword.length() < minPasswordLength) {
            throw new PasswordResetTokenValidationException("Minimum password length is " + minPasswordLength);
        }
        PasswordResetToken passwordResetToken = validatePasswordResetToken(token);
        userDao.saveUserPassword(passwordResetToken.getUserId(), passwordEncoder.encode(newPassword));
    }
}

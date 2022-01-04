package com.rost.productwarehouse.security.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.service.EmailSendingDataService;
import com.rost.productwarehouse.security.PasswordResetToken;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.PasswordResetTokenDao;
import com.rost.productwarehouse.security.dao.UserDao;
import com.rost.productwarehouse.security.exceptions.PasswordChangeValidationException;
import com.rost.productwarehouse.security.exceptions.PasswordResetTokenValidationException;
import com.rost.productwarehouse.security.exceptions.UserNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailSendingDataService emailSendingDataService;

    public UserServiceImpl(UserDao userDao, PasswordResetTokenDao passwordResetTokenDao, PasswordEncoder passwordEncoder, EmailSendingDataService emailSendingDataService) {
        this.userDao = userDao;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.passwordEncoder = passwordEncoder;
        this.emailSendingDataService = emailSendingDataService;
    }

    @Value("${app.passwordResetTokenExpirationMs}")
    private long passwordResetTokenExpirationMs;
    @Value("${app.minPasswordLength}")
    private int minPasswordLength;

    @Override
    public void changePassword(String currentPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (passwordEncoder.matches(currentPassword, userDetails.getPassword())) {
                userDao.saveUserPassword(userDetails.getId(), passwordEncoder.encode(newPassword));
            } else {
                throw new PasswordChangeValidationException("Current password was not confirmed. New password was not changed");
            }
        } else {
            throw new PasswordChangeValidationException("User is not authorized");
        }
    }

    @Override
    public PasswordResetToken createPasswordResetToken(String email) {
        User user = userDao.getByEmail(email).orElseThrow(() -> new UserNotFoundException("User was not found by " + email));
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user.getId());
        passwordResetTokenDao.saveToken(passwordResetToken);
        emailSendingDataService.save(Lists.newArrayList(createEmailPasswordResetSendingData(user, passwordResetToken)));
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

    private EmailSendingData createEmailPasswordResetSendingData(User user, PasswordResetToken passwordResetToken) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(user.getEmail());
        data.setType(EmailSendingData.Type.PASSWORD_RESET_TOKEN);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of("password-reset-link", createResetPasswordLink(passwordResetToken.getToken()),
                "username", user.getUsername(),
                "topic", "Reset Password"));
        return data;
    }

    private String createResetPasswordLink(String token) {
        return "http://localhost:4200/reset-password?token=${token}".replace("${token}", token);
    }
}

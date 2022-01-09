package com.rost.productwarehouse.security.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rost.productwarehouse.email.EmailSendingData;
import com.rost.productwarehouse.email.service.EmailSendingDataService;
import com.rost.productwarehouse.security.PasswordResetToken;
import com.rost.productwarehouse.security.Role;
import com.rost.productwarehouse.security.User;
import com.rost.productwarehouse.security.dao.PasswordResetTokenDao;
import com.rost.productwarehouse.security.dao.UserDao;
import com.rost.productwarehouse.security.exceptions.PasswordChangeValidationException;
import com.rost.productwarehouse.security.exceptions.PasswordResetTokenValidationException;
import com.rost.productwarehouse.security.exceptions.UserNotFoundException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<User> getUsers() {
        List<User> users = userDao.getUsers();
        users.forEach(user -> {
            List<Role> roles = user.getRoles().stream().sorted(Comparator.comparingInt(role -> role.getType().getSort())).collect(Collectors.toList());
            user.setRoles(roles);
        });
        return users;
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userDao.getByUsername(username);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return userDao.getByEmail(email);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void save(User user) {
        boolean isNew = false;
        if (user.getId() <= 0L) {
            isNew = true;
            user.setToNew();
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        }
        validateUser(user);
        userDao.save(user);
        if (isNew) {
            PasswordResetToken passwordResetToken = createPasswordResetToken(user);
            emailSendingDataService.save(Lists.newArrayList(createEmailUserRegistered(user, passwordResetToken)));
        }
    }

    @Override
    public void deleteUser(long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (userId == userDetails.getId()) {
                throw new RuntimeException("You can not delete your account by yourself");
            }
        }
        userDao.deleteUser(userId);
    }

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
        PasswordResetToken passwordResetToken = createPasswordResetToken(user);
        emailSendingDataService.save(Lists.newArrayList(createEmailPasswordResetSendingData(user, passwordResetToken)));
        return passwordResetToken;
    }

    @Override
    public PasswordResetToken validatePasswordResetToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new PasswordResetTokenValidationException("Input token is not valid");
        }
        PasswordResetToken passwordResetToken = passwordResetTokenDao.getToken(token).orElseThrow(() -> new PasswordResetTokenValidationException("Password Reset Token was not found by " + token));
        if (LocalDateTime.now().isAfter(passwordResetToken.getCreated().plusSeconds(passwordResetTokenExpirationMs))) {
            throw new PasswordResetTokenValidationException("Password Reset Token has been expired, please create new one");
        }
        return passwordResetToken;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void changePasswordByToken(String token, String newPassword) {
        if (StringUtils.isEmpty(newPassword) || newPassword.length() < minPasswordLength) {
            throw new PasswordResetTokenValidationException("Minimum password length is " + minPasswordLength);
        }
        PasswordResetToken passwordResetToken = validatePasswordResetToken(token);
        userDao.saveUserPassword(passwordResetToken.getUserId(), passwordEncoder.encode(newPassword));
        passwordResetTokenDao.deleteToken(passwordResetToken.getUserId());
    }

    @Override
    public void deleteExpiredPasswordResetTokens() {
        passwordResetTokenDao.deleteTokens(LocalDateTime.now().minusSeconds(passwordResetTokenExpirationMs));
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

    private void validateUser(User user) {
        if (user.isNew()) {
            if (userDao.getByUsername(user.getUsername()).isPresent()) {
                throw new RuntimeException("Error: Username is exist");
            }

            if (userDao.getByEmail(user.getEmail()).isPresent()) {
                throw new RuntimeException("Error: Email is exist");
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if (user.getId() == userDetails.getId() && !user.getUsername().equals(userDetails.getUsername())) {
                throw new RuntimeException("Username can not be changed");
            }
        }
        if (CollectionUtils.isEmpty(user.getRoles())) {
            throw new RuntimeException(user.getUsername() + " user does not have roles");
        }
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user.getId());
        passwordResetTokenDao.saveToken(passwordResetToken);
        return passwordResetToken;
    }

    private EmailSendingData createEmailUserRegistered(User user, PasswordResetToken passwordResetToken) {
        EmailSendingData data = new EmailSendingData();
        data.setEmail(user.getEmail());
        data.setType(EmailSendingData.Type.USER_REGISTERED);
        data.setStatus(EmailSendingData.Status.NOT_SENT);
        data.setData(ImmutableMap.of("password-reset-link", createResetPasswordLink(passwordResetToken.getToken()),
                "username", user.getUsername(),
                "topic", "Your account has been created"));
        return data;
    }
}

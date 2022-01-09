package com.rost.productwarehouse.security.controller;

import com.rost.productwarehouse.security.*;
import com.rost.productwarehouse.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/getRoles")
    public ResponseEntity<List<Role.Type>> getRoles() {
        return ResponseEntity.ok(Arrays.stream(Role.Type.values()).sorted(Comparator.comparingInt(Role.Type::getSort)).collect(Collectors.toList()));
    }

    @GetMapping("/getUsers")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        String email = resetPasswordRequest.getEmail();
        userService.createPasswordResetToken(email);
        return ResponseEntity.ok("Password Reset Token has been sent by email to " + email);
    }

    @PostMapping("/savePassword")
    public void savePassword(@RequestBody SaveNewPasswordRequest saveNewPasswordRequest) {
        userService.changePasswordByToken(saveNewPasswordRequest.getToken(), saveNewPasswordRequest.getNewPassword());
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword());
    }

    @PostMapping("/saveUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> saveUser(@RequestBody User user) {
        userService.save(user);
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping("/deleteUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> deleteUser(@RequestBody User user) {
        userService.deleteUser(user.getId());
        return ResponseEntity.ok(userService.getUsers());
    }
}

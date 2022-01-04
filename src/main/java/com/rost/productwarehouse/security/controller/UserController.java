package com.rost.productwarehouse.security.controller;

import com.rost.productwarehouse.security.ChangePasswordRequest;
import com.rost.productwarehouse.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam("email") String email) {
        userService.createPasswordResetToken(email);
        return ResponseEntity.ok("Password Reset Token has been sent by email to " + email);
    }

    @PostMapping("/savePassword")
    public void savePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePasswordByToken(changePasswordRequest.getToken(), changePasswordRequest.getNewPassword());
    }

    @PostMapping("/changePassword")
    public void changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword());
    }

}

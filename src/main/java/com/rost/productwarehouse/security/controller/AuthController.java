package com.rost.productwarehouse.security.controller;

import com.rost.productwarehouse.menu.dao.MenuDao;
import com.rost.productwarehouse.security.*;
import com.rost.productwarehouse.security.dao.RoleDao;
import com.rost.productwarehouse.security.dao.UserDao;
import com.rost.productwarehouse.security.jwt.JwtUtils;
import com.rost.productwarehouse.security.service.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final MenuDao menuDao;

    public AuthController(AuthenticationManager authenticationManager, UserDao userDao, RoleDao roleDao, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, MenuDao menuDao) {
        this.authenticationManager = authenticationManager;
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.menuDao = menuDao;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('TEST')")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("test has been passed");
    }

    @PostMapping("/registerUser")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest registerUserRequest) {

        if (userDao.getByUsername(registerUserRequest.getUsername()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is exist"));
        }

        if (userDao.getByEmail(registerUserRequest.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is exist"));
        }

        User user = new User(registerUserRequest.getUsername(),
                registerUserRequest.getEmail(),
                passwordEncoder.encode(registerUserRequest.getPassword()));

        Set<Role> roles = registerUserRequest.getRoles().stream()
                .map(r -> Role.Type.of(r).orElseThrow(() -> new RuntimeException("Error, " + r + "is not found")))
                .map(Role::new)
                .collect(Collectors.toSet());

        user.setRoles(roles);
        userDao.save(user);
        return ResponseEntity.ok(new MessageResponse("User CREATED"));
    }
}

package com.vcarter.ecommerce.controller.v1;

import com.vcarter.ecommerce.dto.LoginDTO;
import com.vcarter.ecommerce.entity.User;
import com.vcarter.ecommerce.security.util.JwtUtil;
import com.vcarter.ecommerce.service.CustomUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private CustomUserService service;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        User registeredUser = service.registerUser(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getAddress(),
                user.getPhoneNumber()
        );
        return ResponseEntity.ok(Map.of(
                "username", registeredUser.getUsername(),
                "email", registeredUser.getEmail(),
                "address", registeredUser.getAddress(),
                "phoneNumber", registeredUser.getPhoneNumber()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails user=service.loadUserByUsername(request.username());

        String token = jwtUtil.generateToken(user.getUsername(), user.getAuthorities().toArray()[0].toString());

        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24) // 24 hours
                .sameSite("Strict")   // Protects against CSRF
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "username", user.getUsername(),
                        "role", user.getAuthorities().toArray()[0].toString()
                ));
    }
}

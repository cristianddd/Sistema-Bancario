package com.bank.authservice.controller;

import com.bank.authservice.dto.AuthRequest;
import com.bank.authservice.dto.AuthResponse;
import com.bank.authservice.dto.RegisterRequestDto;
import com.bank.authservice.dto.UserDto;
import com.bank.authservice.service.AuthenticationService;
import com.bank.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration and authentication")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody @Valid RegisterRequestDto request) {
        return userService.register(request);
    }

    @Operation(summary = "Authenticate a user", description = "Authenticates the user and returns a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            String token = authenticationService.authenticate(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }


    @Operation(summary = "Promote a user to ADMIN role")
    @PostMapping("/{userId}/promote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promoteToAdmin(@PathVariable UUID userId) {
        userService.promoteToAdmin(userId);
    }
}
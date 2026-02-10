package com.bank.authservice.controller;

import com.bank.authservice.dto.UserDto;
import com.bank.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Endpoints related to users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user", description = "Returns information about the currently authenticated user")
    @GetMapping("/me")
    public UserDto getCurrentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication);
    }

    @Operation(summary = "List users", description = "Lists all users in the system. Requires ADMIN role.")
    @GetMapping
    public List<UserDto> listUsers(Authentication authentication) {
        return userService.listUsers(authentication);
    }
}
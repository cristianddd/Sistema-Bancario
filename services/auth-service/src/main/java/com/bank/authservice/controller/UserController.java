package com.bank.authservice.controller;

import com.bank.authservice.dto.UserDto;
import com.bank.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "Promote a user to ADMIN role", description = "Requires ADMIN role.")
    @PostMapping("/{userId}/promote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promoteToAdmin(@PathVariable UUID userId) {
        userService.promoteToAdmin(userId);
    }
}

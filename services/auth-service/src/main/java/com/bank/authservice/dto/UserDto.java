package com.bank.authservice.dto;

import com.bank.authservice.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Tag(name = "User DTO", description = "DTO representing user information")
public class UserDto {
    @Schema(description = "User identifier", example = "1")
    private String id;
    @Schema(description = "Username", example = "johndoe")
    private String username;
    @Schema(description = "Set of roles assigned to the user")
    private Set<Role> roles;
}
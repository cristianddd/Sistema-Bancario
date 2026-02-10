package com.bank.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {

    @Schema(description = "Unique username", example = "johndoe")
    @NotBlank
    private String username;

    @Schema(description = "User's password", example = "s3cr3t")
    @NotBlank
    private String password;

}
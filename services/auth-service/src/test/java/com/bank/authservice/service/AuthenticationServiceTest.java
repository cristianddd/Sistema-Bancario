package com.bank.authservice.service;

import com.bank.authservice.entity.UserEntity;
import com.bank.authservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("cristian");
        user.setPassword("$2a$10$hashed");
    }

    @Test
    void authenticate_shouldReturnJwt_whenCredentialsAreValid() {
        when(userService.findByUsername("cristian")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        String token = authenticationService.authenticate("cristian", "123456");

        assertEquals("jwt-token", token);

        verify(userService).findByUsername("cristian");
        verify(passwordEncoder).matches("123456", user.getPassword());
        verify(jwtUtil).generateToken(user);
        verifyNoMoreInteractions(userService, passwordEncoder, jwtUtil);
    }

    @Test
    void authenticate_shouldThrowBadCredentials_whenUserNotFound() {
        when(userService.findByUsername("cristian")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticate("cristian", "123456")
        );

        assertEquals("Invalid username or password", ex.getMessage());

        verify(userService).findByUsername("cristian");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void authenticate_shouldThrowBadCredentials_whenPasswordDoesNotMatch() {
        when(userService.findByUsername("cristian")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticate("cristian", "wrong")
        );

        assertEquals("Invalid username or password", ex.getMessage());

        verify(userService).findByUsername("cristian");
        verify(passwordEncoder).matches("wrong", user.getPassword());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void authenticate_shouldNotGenerateToken_whenUserNotFound() {
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate("any", "any"));

        verify(jwtUtil, never()).generateToken(ArgumentMatchers.any());
    }

    @Test
    void authenticate_shouldNotGenerateToken_whenPasswordMismatch() {
        when(userService.findByUsername("cristian")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate("cristian", "123"));

        verify(jwtUtil, never()).generateToken(any());
    }
}
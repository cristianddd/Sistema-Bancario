package com.bank.authservice.service;

import com.bank.authservice.dto.RegisterRequestDto;
import com.bank.authservice.dto.UserDto;
import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.UserEntity;
import com.bank.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("cristian");
        user.setEmail("cristian@mail.com");
        user.setPassword("$2a$10$hash");
        user.setRoles(Set.of(Role.USER));
        user.setEnabled(true);
    }

    @Test
    void findByUsername_shouldDelegateToRepository() {
        when(userRepository.findByUsername("cristian")).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findByUsername("cristian");

        assertTrue(result.isPresent());
        assertEquals("cristian", result.get().getUsername());
        verify(userRepository).findByUsername("cristian");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserEntity> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("cristian", result.get(0).getUsername());
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("missing");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.getCurrentUser(auth)
        );

        assertEquals("User not found: missing", ex.getMessage());
        verify(userRepository).findByUsername("missing");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void listUsers_shouldMapEntitiesToDtos() {
        UserEntity u1 = new UserEntity();
        u1.setId(UUID.randomUUID());
        u1.setUsername("u1");
        u1.setRoles(Set.of(Role.USER));

        UserEntity u2 = new UserEntity();
        u2.setId(UUID.randomUUID());
        u2.setUsername("u2");
        u2.setRoles(Set.of(Role.ADMIN));

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserDto> dtos = userService.listUsers(mock(Authentication.class));

        assertEquals(2, dtos.size());

        assertEquals(u1.getId().toString(), dtos.get(0).getId());
        assertEquals("u1", dtos.get(0).getUsername());
        assertEquals(Set.of(Role.USER), dtos.get(0).getRoles());

        assertEquals(u2.getId().toString(), dtos.get(1).getId());
        assertEquals("u2", dtos.get(1).getUsername());
        assertEquals(Set.of(Role.ADMIN), dtos.get(1).getRoles());

        verify(userRepository).findAll();
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_shouldCreateUserAndReturnDto_whenValidRequest() {
        RegisterRequestDto req = mock(RegisterRequestDto.class);
        when(req.getUsername()).thenReturn("newuser");
        when(req.getEmail()).thenReturn("new@mail.com");
        when(req.getPassword()).thenReturn("123456");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed123");

        // capturar entidade salva para validar os campos
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        // simular save retornando a mesma entidade com ID
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto dto = userService.register(req);

        // valida retorno
        assertNotNull(dto);
        assertEquals("newuser", dto.getUsername());
        assertEquals(Set.of(Role.USER), dto.getRoles());
        assertNotNull(dto.getId());

        // valida o que foi salvo
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@mail.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertEquals("newuser", saved.getUsername());
        assertEquals("new@mail.com", saved.getEmail());
        assertEquals("hashed123", saved.getPassword());
        assertEquals(Set.of(Role.USER), saved.getRoles());
        assertTrue(saved.isEnabled());
        assertNotNull(saved.getId());

        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }
}
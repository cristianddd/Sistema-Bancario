package com.bank.authservice.service;

import com.bank.authservice.entity.Role;
import com.bank.authservice.dto.RegisterRequestDto;
import com.bank.authservice.dto.UserDto;
import com.bank.authservice.entity.UserEntity;
import com.bank.authservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();

        UserEntity user = findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<Role> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        return new UserDto(user.getId().toString(), user.getUsername(), roles);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDto> listUsers(Authentication authentication) {

        return findAll().stream()
                .map(user -> new UserDto(
                        user.getId().toString(),
                        user.getUsername(),
                        user.getRoles()
                ))
                .toList();
    }

    @Transactional
    public UserDto register(RegisterRequestDto request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.USER));
        user.setEnabled(true);

        UserEntity saved = userRepository.save(user);

        return new UserDto(
                saved.getId().toString(),
                saved.getUsername(),
                saved.getRoles()
        );
    }


    @Transactional
    @PreAuthorize("hasAuthority('ADMIN')")
    public void promoteToAdmin(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRoles().contains(Role.ADMIN)) {
            return;
        }

        user.getRoles().add(Role.ADMIN);
        userRepository.save(user);
    }
}
package com.bank.authservice.config;

import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.UserEntity;
import com.bank.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;


@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner loadUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";
            String adminPassword = "admin";

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setId(UUID.randomUUID());
                admin.setUsername(adminUsername);
                admin.setEmail("admin@local");
                admin.setRoles(Set.of(Role.ADMIN));
                admin.setEnabled(true);

                admin.setPassword(passwordEncoder.encode(adminPassword));

                userRepository.save(admin);
                log.info("Default admin user created: username='{}', password='{}'", adminUsername, adminPassword);
            } else {
                log.info("Admin user already exists: username='{}'", adminUsername);
            }
        };
    }
}
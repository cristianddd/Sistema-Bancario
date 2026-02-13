package com.bank.authservice.config;

import com.bank.authservice.entity.Role;
import com.bank.authservice.entity.UserEntity;
import com.bank.authservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @ConditionalOnProperty(prefix = "app.bootstrap.admin", name = "enabled", havingValue = "true")
    CommandLineRunner loadUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin.username:admin}") String adminUsername,
            @Value("${app.bootstrap.admin.password:admin}") String adminPassword,
            @Value("${app.bootstrap.admin.email:admin@local}") String adminEmail
    ) {
        return args -> {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                UserEntity admin = new UserEntity();
                admin.setId(UUID.randomUUID());
                admin.setUsername(adminUsername);
                admin.setEmail("admin@local");
                admin.getRoles().add(Role.ADMIN);
                admin.setEnabled(true);
                admin.setPassword(passwordEncoder.encode(adminPassword));

                userRepository.save(admin);
                log.info("Bootstrap admin user created: username='{}'", adminUsername);
            } else {
                log.info("Bootstrap admin user already exists: username='{}'", adminUsername);
            }
        };
    }
}

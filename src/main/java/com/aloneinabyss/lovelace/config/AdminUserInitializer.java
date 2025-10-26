package com.aloneinabyss.lovelace.config;

import com.aloneinabyss.lovelace.model.User;
import com.aloneinabyss.lovelace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!userRepository.findByUsername(adminUsername).isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        User adminUser = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .enabled(true)
                .emailVerified(true)
                .passwordChangedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(adminUser);
        log.info("✅ Admin user created successfully");
    }
}

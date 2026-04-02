package com.negzaoui.stuffing.config;

import com.negzaoui.stuffing.entity.Role;
import com.negzaoui.stuffing.entity.User;
import com.negzaoui.stuffing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initialise un compte ADMIN en dev pour pouvoir tester les endpoints protégés.
 * Détecte et corrige aussi les mots de passe corrompus (hash mal copié dans pgAdmin).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Admin par défaut
            String adminEmail = "admin@stuffing.local";
            String adminPassword = "Admin1234!";

            if (!userRepository.existsByEmail(adminEmail)) {
                var admin = User.builder()
                        .firstName("Admin")
                        .lastName("Stuffing")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                log.warn("Compte ADMIN créé: {} / {}", adminEmail, adminPassword);
            }

            // ─── Détection des mots de passe corrompus ───
            // (hash mal copié dans pgAdmin → ne commence pas par $2a$ ou longueur != 60)
            userRepository.findAll().forEach(user -> {
                String pwd = user.getPassword();
                if (pwd == null || !pwd.startsWith("$2a$") || pwd.length() != 60) {
                    log.error("⚠️  MOT DE PASSE CORROMPU détecté pour {} (email: {}). " +
                                    "Le hash ne ressemble pas à BCrypt : '{}' (longueur={}). " +
                                    "Utilisez /api/auth/register ou PUT /api/admin/users/{}/reset-password pour corriger.",
                            user.getFirstName(), user.getEmail(),
                            pwd != null ? pwd.substring(0, Math.min(pwd.length(), 10)) + "..." : "NULL",
                            pwd != null ? pwd.length() : 0,
                            user.getId());
                }
            });
        };
    }
}

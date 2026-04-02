package com.negzaoui.stuffing.controller;

import com.negzaoui.stuffing.dto.MessageResponse;
import com.negzaoui.stuffing.dto.admin.CreateUserRequest;
import com.negzaoui.stuffing.dto.admin.ResetPasswordRequest;
import com.negzaoui.stuffing.entity.User;
import com.negzaoui.stuffing.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Gestion des utilisateurs", description = "Endpoints ADMIN pour gérer les users (créer, lister, changer mdp)")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Operation(summary = "Lister tous les utilisateurs")
    @GetMapping
    public ResponseEntity<List<User>> listAll() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Operation(summary = "Créer un utilisateur avec un rôle (ADMIN, MANAGER, COLLABORATEUR)")
    @PostMapping
    public ResponseEntity<MessageResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body(
                    MessageResponse.builder().message("Email déjà utilisé : " + req.getEmail()).build()
            );
        }

        var user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();

        userRepository.save(user);
        return ResponseEntity.ok(
                MessageResponse.builder().message("Utilisateur créé : " + req.getEmail() + " [" + req.getRole() + "]").build()
        );
    }

    @Operation(summary = "Réinitialiser le mot de passe d'un utilisateur (par ID)")
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest req
    ) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable (id=" + id + ")"));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(
                MessageResponse.builder().message("Mot de passe réinitialisé pour " + user.getEmail()).build()
        );
    }
}


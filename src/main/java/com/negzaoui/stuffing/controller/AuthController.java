package com.negzaoui.stuffing.controller;

import com.negzaoui.stuffing.dto.*;
import com.negzaoui.stuffing.dto.auth.*;
import com.negzaoui.stuffing.service.AuthenticationService;
import com.negzaoui.stuffing.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Register",
            description = "(Optionnel) Crée un compte utilisateur. Pour une app interne, préférez /api/public/account-requests (demande traitée par l'Admin/Delivery Manager)."
    )
    @ApiResponse(responseCode = "200", description = "Inscription réussie",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur et retourne un token JWT"
    )
    @ApiResponse(responseCode = "200", description = "Connexion réussie",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }


    @Operation(
            summary = "Demande de réinitialisation de mot de passe",
            description = "Génère un token de réinitialisation (en mode PFE/dev, le token est retourné dans la réponse)"
    )
    @ApiResponse(responseCode = "200", description = "Demande traitée",
            content = @Content(schema = @Schema(implementation = PasswordResetRequestResponse.class)))
    @PostMapping("/reset-password/request")
    public ResponseEntity<PasswordResetRequestResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        String token = passwordResetService.createResetToken(request.getEmail());

        // On ne divulgue pas si l'email existe.
        var response = PasswordResetRequestResponse.builder()
                .message("Si l'email existe, un lien de réinitialisation a été généré")
                .token(token) // présent uniquement si l'email existe
                .build();

        return ResponseEntity.ok(response);
    }



    @Operation(
            summary = "Confirmation de réinitialisation de mot de passe",
            description = "Valide le token et met à jour le mot de passe"
    )
    @ApiResponse(responseCode = "200", description = "Mot de passe mis à jour",
            content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<MessageResponse> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder().message("Mot de passe mis à jour").build());
    }
}

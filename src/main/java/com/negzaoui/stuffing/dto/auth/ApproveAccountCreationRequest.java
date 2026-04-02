package com.negzaoui.stuffing.dto.auth;

import com.negzaoui.stuffing.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApproveAccountCreationRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    /**
     * Optionnel: si null, on génère un mot de passe temporaire.
     */
    private String temporaryPassword;
}


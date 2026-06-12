package com.negzaoui.stuffing.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartementRequest {
    @NotBlank(message = "Le nom du département est obligatoire")
    private String name;
}


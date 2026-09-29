package com.sazloop.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ubicación textual de un establecimiento (RF-05). */
public record DireccionRequest(
        @NotBlank @Size(max = 80) String departamento,
        @NotBlank @Size(max = 80) String provincia,
        @NotBlank @Size(max = 80) String distrito,
        @NotBlank @Size(max = 180) String direccionLinea,
        @Size(max = 20) String numero,
        @Size(max = 250) String referencia,
        @Size(max = 10) String codigoPostal
) {
}

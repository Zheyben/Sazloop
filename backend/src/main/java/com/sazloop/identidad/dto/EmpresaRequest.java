package com.sazloop.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de la empresa registrados por su propietario (RF-04). */
public record EmpresaRequest(
        @NotBlank @Size(max = 150) String razonSocial,
        @NotBlank @Size(max = 120) String nombreComercial,
        @NotBlank @Pattern(regexp = "\\d{11}") String ruc
) {
}

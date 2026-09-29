package com.sazloop.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos necesarios para crear una cuenta personal (RF-01). */
public record RegistroUsuarioRequest(
        @NotBlank @Size(max = 80) String nombres,
        @NotBlank @Size(max = 80) String apellidos,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 128) String contrasena,
        @Size(max = 20) String telefono
) {
}

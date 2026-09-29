package com.sazloop.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Credenciales recibidas al iniciar sesión (RF-02). */
public record InicioSesionRequest(
        @NotBlank @Email String email,
        @NotBlank String contrasena
) {
}

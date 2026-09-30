package com.sazloop.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuentaLocalRequest(@NotBlank @Size(max = 80) String nombreUsuario,
                                 @NotBlank @Size(min = 8, max = 128) String contrasena) {}

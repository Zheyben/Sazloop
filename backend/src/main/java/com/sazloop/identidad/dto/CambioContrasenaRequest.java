package com.sazloop.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequest(
        @NotBlank String actual,
        @NotBlank @Size(min = 8, max = 128) String nueva) {}

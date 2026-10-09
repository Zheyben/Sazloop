package com.sazloop.reservas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompraPresencialRequest(@NotBlank String token,
        @NotNull @Valid CompraRequest compra) {}

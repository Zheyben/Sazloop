package com.sazloop.identidad.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos del local físico y su dirección (RF-05). */
public record EstablecimientoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @Size(max = 20) String telefono,
        @NotNull @Valid DireccionRequest direccion
) {
}

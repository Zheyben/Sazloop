package com.sazloop.ofertas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Descripción de lo que podría contener el pack, sin prometer contenido exacto. */
public record PackRequest(@NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 1000) String posiblesProductos,
        List<@NotBlank @Size(max = 80) String> alergenos,
        List<@NotBlank @Size(max = 100) String> restricciones) {}

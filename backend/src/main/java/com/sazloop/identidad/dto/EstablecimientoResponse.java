package com.sazloop.identidad.dto;

import com.sazloop.identidad.domain.Establecimiento;

/** Resumen del establecimiento registrado y su estado de afiliación. */
public record EstablecimientoResponse(
        Long id, String nombre, String distrito, String direccion,
        Establecimiento.EstadoEstablecimiento estado
) {
}

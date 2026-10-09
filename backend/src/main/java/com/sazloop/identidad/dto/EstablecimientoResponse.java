package com.sazloop.identidad.dto;

import com.sazloop.identidad.domain.Establecimiento;

/** Resumen del establecimiento registrado, su estado de afiliación y su cuenta operativa. */
public record EstablecimientoResponse(
        Long id, String nombre, String distrito, String direccion,
        Establecimiento.EstadoEstablecimiento estado, String cuentaTrabajador
) {
}

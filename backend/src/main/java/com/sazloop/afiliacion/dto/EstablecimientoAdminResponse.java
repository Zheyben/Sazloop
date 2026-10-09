package com.sazloop.afiliacion.dto;

import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;

import java.time.LocalDateTime;

/** Estado de cada local para el administrador, con el motivo de su suspensión si la tiene. */
public record EstablecimientoAdminResponse(Long id, String nombre, String empresa, String ruc,
        String distrito, String direccion, EstadoEstablecimiento estado, String motivoSuspension,
        LocalDateTime suspendidoDesde, boolean suspensionAutomatica, boolean solicitudEnRevision) {
}

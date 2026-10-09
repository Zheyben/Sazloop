package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;
import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Datos que necesita el administrador para revisar el local y cada documento. */
public record SolicitudRevisionResponse(Long id, Long establecimientoId, String establecimiento,
        EstadoSolicitud estado, List<DocumentoEnRevision> documentos,
        boolean renovacion, EstadoEstablecimiento estadoLocal, String empresa, String ruc,
        String distrito, String direccion, LocalDateTime fechaSolicitud, String motivoDecision) {
    public record DocumentoEnRevision(Long id, String tipo, String numero,
            LocalDate emision, LocalDate vencimiento, ResultadoRevision resultado, String observaciones) {}
}

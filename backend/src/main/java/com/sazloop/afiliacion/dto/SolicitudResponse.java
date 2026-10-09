package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;

import java.time.LocalDateTime;
import java.util.List;

/** Solicitud del local y documentos ya adjuntados; una renovación sigue a una solicitud aprobada. */
public record SolicitudResponse(Long id, Long establecimientoId, EstadoSolicitud estado,
                                List<DocumentoResponse> documentos, String motivoDecision,
                                boolean renovacion, LocalDateTime fechaSolicitud) {
}

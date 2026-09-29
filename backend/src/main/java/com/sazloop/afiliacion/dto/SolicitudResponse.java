package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.SolicitudAfiliacion.EstadoSolicitud;

import java.util.List;

/** Solicitud del local y documentos ya adjuntados. */
public record SolicitudResponse(Long id, Long establecimientoId, EstadoSolicitud estado,
                                List<DocumentoResponse> documentos) {
}

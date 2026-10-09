package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import com.sazloop.afiliacion.service.VigenciaDocumentosService.EstadoVigencia;
import com.sazloop.identidad.domain.Establecimiento.EstadoEstablecimiento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Situación documental de un local para su propietario: documentos en vigor,
 * suspensión vigente y los tipos que debe presentar para renovar.
 */
public record DocumentacionResponse(Long establecimientoId, String establecimiento,
        EstadoEstablecimiento estado, String motivoSuspension, LocalDateTime suspendidoDesde,
        List<DocumentoVigente> documentos, List<TipoDocumento> tiposVencidos,
        List<TipoDocumento> tiposRenovables, SolicitudResponse solicitud) {
    public record DocumentoVigente(Long id, TipoDocumento tipo, String numero,
            LocalDate vencimiento, EstadoVigencia vigencia) {}
}

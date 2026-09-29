package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;

import java.time.LocalDate;

/** Resumen sin ruta física ni acceso público al archivo privado. */
public record DocumentoResponse(Long id, TipoDocumento tipo, String numero,
                                LocalDate fechaEmision, LocalDate fechaVencimiento) {
}

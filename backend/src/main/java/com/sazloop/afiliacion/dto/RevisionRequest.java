package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import com.sazloop.afiliacion.domain.DocumentoEstablecimiento.TipoDocumento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** El revisor puede corregir los datos transcritos al abrir el archivo. */
public record RevisionRequest(@NotNull ResultadoRevision resultado, @Size(max = 500) String observaciones,
        TipoDocumento tipo, @Size(max = 80) String numero,
        LocalDate emision, LocalDate vencimiento) {
    public RevisionRequest(ResultadoRevision resultado, String observaciones) {
        this(resultado, observaciones, null, null, null, null);
    }
}

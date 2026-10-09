package com.sazloop.afiliacion.dto;

import com.sazloop.afiliacion.domain.RevisionDocumento.ResultadoRevision;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Revisión de todos los documentos y decisión sobre la solicitud en un solo paso.
 * La solicitud se aprueba únicamente si todos los documentos se aprueban.
 */
public record ResolucionRequest(@NotEmpty List<@Valid Documento> documentos,
                                @Size(max = 500) String comentario) {
    public record Documento(@NotNull Long id, @NotNull ResultadoRevision resultado,
                            @Size(max = 500) String observaciones) {}
}

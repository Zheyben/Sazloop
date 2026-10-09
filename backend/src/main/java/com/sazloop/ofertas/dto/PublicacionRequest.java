package com.sazloop.ofertas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/** Datos de un pack nuevo y su primera oferta, guardados juntos. */
public record PublicacionRequest(@NotNull @Valid PackRequest pack,
        @NotNull LocalDateTime inicioRecojo, @NotNull LocalDateTime finRecojo,
        @NotEmpty @Valid List<OfertaRequest.VarianteRequest> variantes) {}

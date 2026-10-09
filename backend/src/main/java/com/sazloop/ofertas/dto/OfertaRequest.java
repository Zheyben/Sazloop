package com.sazloop.ofertas.dto;

import com.sazloop.ofertas.domain.VariantePack.TamanioVariante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Horario y variantes de la única oferta activa del local. */
public record OfertaRequest(@NotNull Long packId, @NotNull LocalDateTime inicioRecojo,
        @NotNull LocalDateTime finRecojo,
        @NotEmpty List<@Valid VarianteRequest> variantes) {
    public record VarianteRequest(@NotNull TamanioVariante tamanio,
            @NotNull @DecimalMin("0.01") BigDecimal precioReferencial,
            @NotNull @DecimalMin("0.01") BigDecimal precioDescuento,
            @Min(1) int stock) {}
}

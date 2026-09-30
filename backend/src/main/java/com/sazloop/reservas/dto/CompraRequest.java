package com.sazloop.reservas.dto;

import com.sazloop.transacciones.domain.Pago.MedioPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** Compra remota con pago simulado; cada variante se descuenta de su stock. */
public record CompraRequest(@NotNull Long ofertaId, @NotNull MedioPago medioPago,
        @NotEmpty List<@Valid Linea> lineas) {
    public record Linea(@NotNull Long varianteId, @Min(1) int cantidad) {}
}
